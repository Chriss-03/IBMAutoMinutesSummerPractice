import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AIResultResponse,
  ActionItemResponse,
  AttendeeResponse,
  CreateAttendeeRequest,
  CreateMeetingRequest,
  MeetingResponse,
  PromptTemplateRequest,
  PromptTemplateResponse,
  TranscriptRequest,
  TranscriptResponse,
  UpdateActionItemRequest,
  UpdateAttendeeRequest,
  UpdateMeetingRequest
} from '../models/autominutes.models';

@Injectable({ providedIn: 'root' })
export class AutominutesApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api';

  getMeetings(): Observable<MeetingResponse[]> {
    return this.http.get<MeetingResponse[]>(`${this.baseUrl}/meetings`);
  }

  createMeeting(request: CreateMeetingRequest): Observable<MeetingResponse> {
    return this.http.post<MeetingResponse>(`${this.baseUrl}/meetings`, request);
  }

  updateMeeting(meetingId: string, request: UpdateMeetingRequest): Observable<MeetingResponse> {
    return this.http.put<MeetingResponse>(`${this.baseUrl}/meetings/${meetingId}`, request);
  }

  deleteMeeting(meetingId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/meetings/${meetingId}`);
  }

  getAttendees(meetingId: string): Observable<AttendeeResponse[]> {
    return this.http.get<AttendeeResponse[]>(`${this.baseUrl}/meetings/${meetingId}/attendees`);
  }

  addAttendee(meetingId: string, request: CreateAttendeeRequest): Observable<AttendeeResponse> {
    return this.http.post<AttendeeResponse>(`${this.baseUrl}/meetings/${meetingId}/attendees`, request);
  }

  updateAttendee(attendeeId: string, request: UpdateAttendeeRequest): Observable<AttendeeResponse> {
    return this.http.put<AttendeeResponse>(`${this.baseUrl}/attendees/${attendeeId}`, request);
  }

  deleteAttendee(attendeeId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/attendees/${attendeeId}`);
  }

  saveTranscript(meetingId: string, request: TranscriptRequest): Observable<TranscriptResponse> {
    return this.http.put<TranscriptResponse>(`${this.baseUrl}/meetings/${meetingId}/transcript`, request);
  }

  getTranscript(meetingId: string): Observable<TranscriptResponse> {
    return this.http.get<TranscriptResponse>(`${this.baseUrl}/meetings/${meetingId}/transcript`);
  }

  processMeeting(meetingId: string): Observable<AIResultResponse> {
    return this.http.post<AIResultResponse>(`${this.baseUrl}/meetings/${meetingId}/process`, {});
  }

  reprocessMeeting(meetingId: string): Observable<AIResultResponse> {
    return this.http.post<AIResultResponse>(`${this.baseUrl}/meetings/${meetingId}/reprocess`, {});
  }

  getLatestResult(meetingId: string): Observable<AIResultResponse> {
    return this.http.get<AIResultResponse>(`${this.baseUrl}/meetings/${meetingId}/ai-results/latest`);
  }

  getResultHistory(meetingId: string): Observable<AIResultResponse[]> {
    return this.http.get<AIResultResponse[]>(`${this.baseUrl}/meetings/${meetingId}/ai-results`);
  }

  getActionItems(meetingId: string): Observable<ActionItemResponse[]> {
    return this.http.get<ActionItemResponse[]>(`${this.baseUrl}/meetings/${meetingId}/action-items`);
  }

  updateActionItem(actionItemId: string, request: UpdateActionItemRequest): Observable<ActionItemResponse> {
    return this.http.put<ActionItemResponse>(`${this.baseUrl}/action-items/${actionItemId}`, request);
  }

  getActivePromptTemplate(): Observable<PromptTemplateResponse> {
    return this.http.get<PromptTemplateResponse>(`${this.baseUrl}/prompt-templates/active`);
  }

  savePromptTemplate(request: PromptTemplateRequest, promptTemplateId?: string): Observable<PromptTemplateResponse> {
    if (promptTemplateId) {
      return this.http.put<PromptTemplateResponse>(`${this.baseUrl}/prompt-templates/${promptTemplateId}`, request);
    }

    return this.http.post<PromptTemplateResponse>(`${this.baseUrl}/prompt-templates`, request);
  }
}
