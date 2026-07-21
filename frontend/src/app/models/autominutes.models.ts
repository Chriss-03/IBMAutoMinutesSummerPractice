export type ProcessingStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
export type ActionItemStatus = 'OPEN' | 'IN_PROGRESS' | 'DONE' | 'UNKNOWN';

export interface MeetingResponse {
  id: string;
  title: string;
  meetingDateTime: string;
  description: string | null;
  processingStatus: ProcessingStatus;
  transcriptPresent: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMeetingRequest {
  title: string;
  meetingDateTime: string;
  description: string;
}

export interface UpdateMeetingRequest extends CreateMeetingRequest {}

export interface AttendeeResponse {
  id: string;
  name: string;
  email: string | null;
  role: string | null;
  meetingId: string;
}

export interface CreateAttendeeRequest {
  name: string;
  email: string;
  role: string;
}

export interface UpdateAttendeeRequest extends CreateAttendeeRequest {}

export interface TranscriptRequest {
  content: string;
}

export interface TranscriptResponse {
  id: string;
  meetingId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface ActionItemResponse {
  id: string;
  description: string;
  proposedAssignee: string | null;
  deadline: string | null;
  status: ActionItemStatus;
  meetingId: string;
  aiResultId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateActionItemRequest {
  description: string;
  proposedAssignee: string;
  deadline: string | null;
  status: ActionItemStatus;
}

export interface AIResultResponse {
  id: string;
  meetingId: string;
  promptTemplateId: string | null;
  summary: string | null;
  detailedSummary: string | null;
  keyDiscussionPoints: string | null;
  decisions: string | null;
  followUpNotes: string | null;
  versionNumber: number;
  latest: boolean;
  createdAt: string;
  actionItems: ActionItemResponse[];
}

export interface PromptTemplateResponse {
  id: string;
  name: string;
  promptText: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PromptTemplateRequest {
  name: string;
  promptText: string;
  active: boolean;
}

export interface FieldErrorResponse {
  field: string;
  message: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: FieldErrorResponse[];
}
