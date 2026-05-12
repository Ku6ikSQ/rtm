export type UserRole = 'USER' | 'MODERATOR' | 'ADMIN'
export type ArtistRole = 'MAIN' | 'FEATURED' | 'PRODUCER'

export interface User {
  id: string
  username: string
  email: string
  role: UserRole
  createdAt: string
  isActive: boolean
  avatarUrl?: string
}

export interface Artist {
  id: string
  stageName: string
  realName?: string
  bio?: string
  country?: string
  imageUrl?: string
}

export interface Genre {
  id: string
  name: string
  slug: string
  description?: string
  parentId?: string
  albumCount?: number
}

export interface Platform {
  id: string
  name: string
  logoUrl?: string
}

export interface Track {
  id: string
  title: string
  albumId: string
  trackNumber: number
  durationSeconds: number
}

export interface AlbumLink {
  albumId: string
  platformId: string
  url: string
  platform?: Platform
}

export interface AlbumArtist {
  albumId: string
  artistId: string
  role: ArtistRole
  order: number
  artist?: Artist
}

export interface Review {
  id: string
  albumId: string
  authorId: string
  score: number
  reviewText: string
  createdAt: string
  updatedAt: string
  author?: Pick<User, 'id' | 'username' | 'avatarUrl'>
}

export interface Album {
  id: string
  title: string
  description?: string
  releaseYear: number
  coverUrl?: string
  avgRating: number
  reviewCount: number
  createdAt: string
  artists: AlbumArtist[]
  genres: Genre[]
  tracks: Track[]
  links: AlbumLink[]
}

// DTO types for forms
export interface LoginDto {
  email: string
  password: string
}

export interface RegisterDto {
  username: string
  email: string
  password: string
  passwordConfirm: string
}

export interface CreateTrackInput {
  title: string
  durationSeconds?: number
}

export interface CreateLinkInput {
  platformId: string
  url: string
}

export interface CreateAlbumDto {
  title: string
  description?: string
  releaseYear: number
  artistIds: string[]
  genreIds: string[]
  tracks?: CreateTrackInput[]
  links?: CreateLinkInput[]
}

export interface UpdateAlbumDto {
  title?: string
  description?: string
  releaseYear?: number
  artistIds?: string[]
  genreIds?: string[]
  tracks?: CreateTrackInput[]
  links?: CreateLinkInput[]
}

export interface CreateReviewDto {
  score: number
  reviewText: string
}

export interface UpdateReviewDto extends Partial<CreateReviewDto> {}

export interface UpdateProfileDto {
  username?: string
  about?: string
}

export interface ChangePasswordDto {
  currentPassword: string
  newPassword: string
  newPasswordConfirm: string
}

export interface CreateArtistDto {
  stageName: string
  realName?: string
  bio?: string
  country?: string
}

export interface CreateGenreDto {
  name: string
  slug: string
  description?: string
  parentId?: string
}

export interface CreatePlatformDto {
  name: string
  logoFile?: File
}

// Auth responses
export interface AuthTokens {
  accessToken: string
  refreshToken: string
}

// Filters
export interface AlbumFilters {
  q?: string
  genreId?: string
  artistId?: string
  yearFrom?: number
  yearTo?: number
  ratingMin?: number
  ratingMax?: number
  sort?: 'rating' | 'year' | 'createdAt'
  order?: 'asc' | 'desc'
  page?: number
  size?: number
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}
