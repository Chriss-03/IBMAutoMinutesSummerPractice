import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import {
  AIResultResponse,
  ActionItemResponse,
  ActionItemStatus,
  AttendeeResponse,
  CreateAttendeeRequest,
  CreateMeetingRequest,
  ErrorResponse,
  MeetingResponse,
  ProcessingStatus,
  PromptTemplateRequest,
  PromptTemplateResponse,
  UpdateActionItemRequest,
  UpdateMeetingRequest
} from './models/autominutes.models';
import { AutominutesApiService } from './services/autominutes-api.service';

interface MeetingDraft {
  title: string;
  meetingDateTime: string;
  description: string;
  transcript: string;
  attendees: CreateAttendeeRequest[];
}

interface MeetingEditDraft {
  title: string;
  meetingDateTime: string;
  description: string;
}

interface ActionItemEditDraft {
  description: string;
  proposedAssignee: string;
  deadline: string;
  status: ActionItemStatus;
}

interface PromptTemplateDraft {
  id: string | null;
  name: string;
  promptText: string;
  active: boolean;
}

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly api = inject(AutominutesApiService);

  readonly actionStatuses: ActionItemStatus[] = ['OPEN', 'IN_PROGRESS', 'DONE', 'UNKNOWN'];
  readonly sampleTranscript = 'Andrei: We need the AutoMinutes dashboard ready by Friday.\nTony: I will finish the frontend meeting form and connect it to the backend by Thursday.\nIoana: I will test the transcript upload flow tomorrow.\nAndrei: Decision: we will demo the app next Monday if QA passes.\nTony: I also need to display the generated summary and action items in the frontend.\nAndrei: Good. Tony, please finish the frontend integration by Friday.\nIoana: I will send the QA report by Monday morning.';

  meetings: MeetingResponse[] = [];
  attendees: AttendeeResponse[] = [];
  actionItems: ActionItemResponse[] = [];
  resultHistory: AIResultResponse[] = [];

  selectedMeeting: MeetingResponse | null = null;
  latestResult: AIResultResponse | null = null;
  activePrompt: PromptTemplateResponse | null = null;

  meetingDraft: MeetingDraft = this.createEmptyMeetingDraft();
  meetingEditDraft: MeetingEditDraft = this.createEmptyMeetingEditDraft();
  attendeeDraft: CreateAttendeeRequest = this.createEmptyAttendeeDraft();
  actionEditDraft: ActionItemEditDraft = this.createEmptyActionEditDraft();
  promptDraft: PromptTemplateDraft = this.createEmptyPromptDraft();
  transcriptDraft = '';

  loadingMeetings = false;
  loadingDetail = false;
  savingMeeting = false;
  savingTranscript = false;
  processingMeetingId: string | null = null;
  savingActionId: string | null = null;
  editingActionId: string | null = null;
  savingPrompt = false;

  errorMessage = '';
  successMessage = '';

  async ngOnInit(): Promise<void> {
    await Promise.all([this.loadMeetings(), this.loadActivePrompt()]);
  }

  async loadMeetings(): Promise<void> {
    this.loadingMeetings = true;
    this.errorMessage = '';

    try {
      const meetings = await firstValueFrom(this.api.getMeetings());
      this.meetings = meetings.sort((a, b) => this.timeValue(b.createdAt) - this.timeValue(a.createdAt));

      if (!this.selectedMeeting && this.meetings.length > 0) {
        await this.selectMeeting(this.meetings[0]);
      }

      if (this.selectedMeeting) {
        const refreshed = this.meetings.find((meeting) => meeting.id === this.selectedMeeting?.id);
        if (refreshed) {
          this.selectedMeeting = refreshed;
          this.syncMeetingEditDraft(refreshed);
        }
      }
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not load meetings. Check that the backend is running on port 8080.');
    } finally {
      this.loadingMeetings = false;
    }
  }

  async selectMeeting(meeting: MeetingResponse): Promise<void> {
    const meetingId = meeting.id;

    this.selectedMeeting = meeting;
    this.syncMeetingEditDraft(meeting);
    this.loadingDetail = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.editingActionId = null;

    this.attendees = [];
    this.transcriptDraft = '';
    this.latestResult = null;
    this.resultHistory = [];
    this.actionItems = [];

    try {
      await Promise.allSettled([
        this.loadAttendeesForMeeting(meetingId),
        this.loadTranscriptForMeeting(meetingId),
        this.loadLatestResultForMeeting(meetingId),
        this.loadResultHistoryForMeeting(meetingId),
        this.loadActionItemsForMeeting(meetingId)
      ]);
    } finally {
      if (this.selectedMeeting?.id === meetingId) {
        this.loadingDetail = false;
      }
    }
  }

  async createMeeting(processAfterCreate: boolean): Promise<void> {
    if (!this.meetingDraft.title.trim()) {
      this.errorMessage = 'Meeting title is required.';
      return;
    }

    if (!this.meetingDraft.meetingDateTime) {
      this.errorMessage = 'Meeting date and time are required.';
      return;
    }

    this.savingMeeting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request: CreateMeetingRequest = {
      title: this.meetingDraft.title.trim(),
      meetingDateTime: this.meetingDraft.meetingDateTime,
      description: this.meetingDraft.description.trim()
    };

    try {
      const created = await firstValueFrom(this.api.createMeeting(request));
      const attendees = this.meetingDraft.attendees.filter((attendee) => attendee.name.trim());

      for (const attendee of attendees) {
        await firstValueFrom(this.api.addAttendee(created.id, {
          name: attendee.name.trim(),
          email: attendee.email.trim(),
          role: attendee.role.trim()
        }));
      }

      if (this.meetingDraft.transcript.trim()) {
        await firstValueFrom(this.api.saveTranscript(created.id, { content: this.meetingDraft.transcript.trim() }));
      }

      await this.loadMeetings();
      const selected = this.meetings.find((meeting) => meeting.id === created.id) ?? created;
      await this.selectMeeting(selected);

      if (processAfterCreate && this.meetingDraft.transcript.trim()) {
        await this.processSelectedMeeting(false);
      } else {
        this.successMessage = 'Meeting saved.';
      }

      this.meetingDraft = this.createEmptyMeetingDraft();
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not create the meeting.');
    } finally {
      this.savingMeeting = false;
    }
  }

  async saveSelectedMeeting(): Promise<void> {
    if (!this.selectedMeeting) {
      return;
    }

    if (!this.meetingEditDraft.title.trim()) {
      this.errorMessage = 'Meeting title is required.';
      return;
    }

    const request: UpdateMeetingRequest = {
      title: this.meetingEditDraft.title.trim(),
      meetingDateTime: this.meetingEditDraft.meetingDateTime,
      description: this.meetingEditDraft.description.trim()
    };

    this.savingMeeting = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const updated = await firstValueFrom(this.api.updateMeeting(this.selectedMeeting.id, request));
      this.selectedMeeting = updated;
      this.syncMeetingEditDraft(updated);
      await this.loadMeetings();
      this.successMessage = 'Meeting details updated.';
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not update the meeting.');
    } finally {
      this.savingMeeting = false;
    }
  }

  async deleteSelectedMeeting(): Promise<void> {
    if (!this.selectedMeeting) {
      return;
    }

    const meetingId = this.selectedMeeting.id;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      await firstValueFrom(this.api.deleteMeeting(meetingId));
      this.selectedMeeting = null;
      this.latestResult = null;
      this.attendees = [];
      this.actionItems = [];
      this.resultHistory = [];
      this.transcriptDraft = '';
      await this.loadMeetings();
      this.successMessage = 'Meeting deleted.';
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not delete the meeting.');
    }
  }

  addDraftAttendee(): void {
    this.meetingDraft.attendees.push(this.createEmptyAttendeeDraft());
  }

  removeDraftAttendee(index: number): void {
    this.meetingDraft.attendees.splice(index, 1);

    if (this.meetingDraft.attendees.length === 0) {
      this.addDraftAttendee();
    }
  }

  useSampleTranscript(): void {
    this.meetingDraft.transcript = this.sampleTranscript;
  }

  useSampleForSelectedTranscript(): void {
    this.transcriptDraft = this.sampleTranscript;
  }

  async addAttendeeToSelectedMeeting(): Promise<void> {
    if (!this.selectedMeeting || !this.attendeeDraft.name.trim()) {
      this.errorMessage = 'Participant name is required.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    try {
      const attendee = await firstValueFrom(this.api.addAttendee(this.selectedMeeting.id, {
        name: this.attendeeDraft.name.trim(),
        email: this.attendeeDraft.email.trim(),
        role: this.attendeeDraft.role.trim()
      }));

      this.attendees = [...this.attendees, attendee];
      this.attendeeDraft = this.createEmptyAttendeeDraft();
      this.successMessage = 'Participant added.';
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not add the participant.');
    }
  }

  async removeAttendee(attendee: AttendeeResponse): Promise<void> {
    this.errorMessage = '';
    this.successMessage = '';

    try {
      await firstValueFrom(this.api.deleteAttendee(attendee.id));
      this.attendees = this.attendees.filter((item) => item.id !== attendee.id);
      this.successMessage = 'Participant removed.';
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not remove the participant.');
    }
  }

  async saveTranscript(): Promise<void> {
    if (!this.selectedMeeting) {
      return;
    }

    if (!this.transcriptDraft.trim()) {
      this.errorMessage = 'Transcript content is required.';
      return;
    }

    this.savingTranscript = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      await firstValueFrom(this.api.saveTranscript(this.selectedMeeting.id, { content: this.transcriptDraft.trim() }));
      await this.loadMeetings();
      this.successMessage = 'Transcript saved.';
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not save the transcript.');
    } finally {
      this.savingTranscript = false;
    }
  }

  async processSelectedMeeting(reprocess: boolean): Promise<void> {
    if (!this.selectedMeeting) {
      return;
    }

    if (!this.transcriptDraft.trim()) {
      this.errorMessage = 'Add a transcript before generating minutes.';
      return;
    }

    this.processingMeetingId = this.selectedMeeting.id;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      await firstValueFrom(this.api.saveTranscript(this.selectedMeeting.id, { content: this.transcriptDraft.trim() }));
      const result = await firstValueFrom(
        reprocess
          ? this.api.reprocessMeeting(this.selectedMeeting.id)
          : this.api.processMeeting(this.selectedMeeting.id)
      );

      this.latestResult = result;
      this.actionItems = result.actionItems ?? [];
      await this.refreshSelectedMeeting();
      await this.refreshResultHistory();
      this.successMessage = reprocess ? 'Minutes regenerated.' : 'Minutes generated.';
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not generate minutes. Check the backend and local model.');
      await this.refreshSelectedMeeting();
    } finally {
      this.processingMeetingId = null;
    }
  }

  async refreshResultHistory(): Promise<void> {
    if (!this.selectedMeeting) {
      return;
    }

    await this.loadResultHistoryForMeeting(this.selectedMeeting.id);
  }

  private async loadAttendeesForMeeting(meetingId: string): Promise<void> {
    const attendees = await this.safeLoad(firstValueFrom(this.api.getAttendees(meetingId)), []);

    if (this.selectedMeeting?.id === meetingId) {
      this.attendees = attendees;
    }
  }

  private async loadTranscriptForMeeting(meetingId: string): Promise<void> {
    const transcript = await this.safeLoad(firstValueFrom(this.api.getTranscript(meetingId)), null);

    if (this.selectedMeeting?.id === meetingId) {
      this.transcriptDraft = transcript?.content ?? '';
    }
  }

  private async loadLatestResultForMeeting(meetingId: string): Promise<void> {
    const latest = await this.safeLoad(firstValueFrom(this.api.getLatestResult(meetingId)), null);

    if (this.selectedMeeting?.id === meetingId) {
      this.latestResult = latest;
    }
  }

  private async loadResultHistoryForMeeting(meetingId: string): Promise<void> {
    const history = await this.safeLoad(firstValueFrom(this.api.getResultHistory(meetingId)), []);

    if (this.selectedMeeting?.id === meetingId) {
      this.resultHistory = history.sort((a, b) => b.versionNumber - a.versionNumber);
    }
  }

  private async loadActionItemsForMeeting(meetingId: string): Promise<void> {
    const actionItems = await this.safeLoad(firstValueFrom(this.api.getActionItems(meetingId)), []);

    if (this.selectedMeeting?.id === meetingId) {
      this.actionItems = actionItems;
    }
  }

  showHistoryResult(result: AIResultResponse): void {
    this.latestResult = result;
    this.actionItems = result.actionItems ?? [];
  }

  beginEditAction(actionItem: ActionItemResponse): void {
    this.editingActionId = actionItem.id;
    this.actionEditDraft = {
      description: actionItem.description,
      proposedAssignee: actionItem.proposedAssignee ?? '',
      deadline: actionItem.deadline ?? '',
      status: actionItem.status
    };
  }

  cancelEditAction(): void {
    this.editingActionId = null;
    this.actionEditDraft = this.createEmptyActionEditDraft();
  }

  async saveActionItem(actionItem: ActionItemResponse): Promise<void> {
    const request: UpdateActionItemRequest = {
      description: this.actionEditDraft.description.trim(),
      proposedAssignee: this.actionEditDraft.proposedAssignee.trim(),
      deadline: this.actionEditDraft.deadline || null,
      status: this.actionEditDraft.status
    };

    if (!request.description) {
      this.errorMessage = 'Action item description is required.';
      return;
    }

    this.savingActionId = actionItem.id;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const updated = await firstValueFrom(this.api.updateActionItem(actionItem.id, request));
      this.actionItems = this.actionItems.map((item) => item.id === updated.id ? updated : item);

      if (this.latestResult) {
        this.latestResult = {
          ...this.latestResult,
          actionItems: this.latestResult.actionItems.map((item) => item.id === updated.id ? updated : item)
        };
      }

      this.cancelEditAction();
      this.successMessage = 'Action item updated.';
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not update the action item.');
    } finally {
      this.savingActionId = null;
    }
  }

  async loadActivePrompt(): Promise<void> {
    try {
      const prompt = await firstValueFrom(this.api.getActivePromptTemplate());
      this.activePrompt = prompt;
      this.promptDraft = {
        id: prompt.id,
        name: prompt.name,
        promptText: prompt.promptText,
        active: prompt.active
      };
    } catch {
      this.activePrompt = null;
    }
  }

  async savePromptTemplate(): Promise<void> {
    if (!this.promptDraft.name.trim() || !this.promptDraft.promptText.trim()) {
      this.errorMessage = 'Prompt name and content are required.';
      return;
    }

    const request: PromptTemplateRequest = {
      name: this.promptDraft.name.trim(),
      promptText: this.promptDraft.promptText.trim(),
      active: this.promptDraft.active
    };

    this.savingPrompt = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const saved = await firstValueFrom(this.api.savePromptTemplate(request, this.promptDraft.id ?? undefined));
      this.activePrompt = saved;
      this.promptDraft = {
        id: saved.id,
        name: saved.name,
        promptText: saved.promptText,
        active: saved.active
      };
      this.successMessage = 'Prompt template saved.';
    } catch (error) {
      this.errorMessage = this.formatError(error, 'Could not save the prompt template.');
    } finally {
      this.savingPrompt = false;
    }
  }

  isProcessing(meeting?: MeetingResponse | null): boolean {
    const meetingId = meeting?.id ?? this.selectedMeeting?.id;
    return Boolean(meetingId && this.processingMeetingId === meetingId);
  }

  statusClass(status: ProcessingStatus | ActionItemStatus | string | null | undefined): string {
    return (status ?? 'pending').toLowerCase().replace('_', '-');
  }

  initials(value: string | null | undefined): string {
    const words = (value ?? '').trim().split(/\s+/).filter(Boolean);

    if (words.length === 0) {
      return 'AM';
    }

    return words.slice(0, 2).map((word) => word[0]?.toUpperCase()).join('');
  }

  lines(value: string | null | undefined): string[] {
    return (value ?? '')
      .split(/\n|•|;/)
      .map((line) => line.trim())
      .filter(Boolean);
  }

  openActionCount(): number {
    return this.actionItems.filter((item) => item.status !== 'DONE').length;
  }

  completedActionCount(): number {
    return this.actionItems.filter((item) => item.status === 'DONE').length;
  }

  trackById(_: number, item: { id: string }): string {
    return item.id;
  }

  trackByIndex(index: number): number {
    return index;
  }

  private async refreshSelectedMeeting(): Promise<void> {
    await this.loadMeetings();

    if (this.selectedMeeting) {
      const selected = this.meetings.find((meeting) => meeting.id === this.selectedMeeting?.id);
      if (selected) {
        this.selectedMeeting = selected;
        this.syncMeetingEditDraft(selected);
      }
    }
  }

  private createEmptyMeetingDraft(): MeetingDraft {
    return {
      title: '',
      meetingDateTime: this.defaultDateTimeLocal(),
      description: '',
      transcript: '',
      attendees: [this.createEmptyAttendeeDraft()]
    };
  }

  private createEmptyMeetingEditDraft(): MeetingEditDraft {
    return {
      title: '',
      meetingDateTime: this.defaultDateTimeLocal(),
      description: ''
    };
  }

  private createEmptyAttendeeDraft(): CreateAttendeeRequest {
    return { name: '', email: '', role: '' };
  }

  private createEmptyActionEditDraft(): ActionItemEditDraft {
    return {
      description: '',
      proposedAssignee: '',
      deadline: '',
      status: 'OPEN'
    };
  }

  private createEmptyPromptDraft(): PromptTemplateDraft {
    return {
      id: null,
      name: 'Meeting minutes extractor',
      promptText: '',
      active: true
    };
  }

  private syncMeetingEditDraft(meeting: MeetingResponse): void {
    this.meetingEditDraft = {
      title: meeting.title,
      meetingDateTime: this.toDateTimeLocal(meeting.meetingDateTime),
      description: meeting.description ?? ''
    };
  }

  private defaultDateTimeLocal(): string {
    const date = new Date();
    date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return date.toISOString().slice(0, 16);
  }

  private toDateTimeLocal(value: string): string {
    return value?.slice(0, 16) ?? this.defaultDateTimeLocal();
  }

  private timeValue(value: string): number {
    return new Date(value).getTime() || 0;
  }

  private async safeLoad<T>(promise: Promise<T>, fallback: T): Promise<T> {
    try {
      return await promise;
    } catch {
      return fallback;
    }
  }

  private formatError(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const apiError = error.error as ErrorResponse | string | null;

      if (typeof apiError === 'string' && apiError.trim()) {
        return apiError;
      }

      if (apiError && typeof apiError !== 'string') {
        const fieldMessages = apiError.fieldErrors?.map((field) => `${field.field}: ${field.message}`).join(' ');
        return [apiError.message, fieldMessages].filter(Boolean).join(' ');
      }
    }

    return fallback;
  }
}
