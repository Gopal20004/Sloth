export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type User = {
  id: number;
  displayName: string;
  email: string;
  createdAt: string;
};

export type PublicUser = Pick<User, "id" | "displayName" | "createdAt">;

export type Game = {
  id: number;
  slug: string;
  name: string;
  description: string;
  coverUrl: string | null;
};

export type Post = {
  id: number;
  gameSlug: string;
  authorId: number;
  authorName: string;
  title: string;
  body: string;
  createdAt: string;
};

export type Reply = {
  id: number;
  authorId: number;
  authorName: string;
  body: string;
  createdAt: string;
};

export type ChatMessage = {
  id: number;
  gameSlug: string;
  senderId: number;
  senderName: string;
  body: string;
  sentAt: string;
};

export type Video = {
  id: number;
  ownerId: number;
  ownerName: string;
  gameSlug: string | null;
  title: string;
  fileUrl: string;
  contentType: string;
  sizeBytes: number;
  createdAt: string;
};

export type Server = {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  createdAt: string;
};

export type ServerMember = {
  userId: number;
  displayName: string;
  joinedAt: string;
};

export type Invite = {
  id: number;
  code: string;
  expiresAt: string;
};
