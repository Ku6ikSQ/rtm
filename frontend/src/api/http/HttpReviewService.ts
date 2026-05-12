import type { IReviewService } from '@/api/interfaces/IReviewService'
import type { Review, CreateReviewDto, UpdateReviewDto } from '@/types/entities'
import { del, get, patch, post } from './apiClient'

interface BackendReviewResponse {
  id: string
  albumId: string
  authorId: string
  score: number
  reviewText: string
  createdAt: string
  updatedAt: string
  authorUsername: string
  authorImageUrl: string | null
}

interface BackendPage<T> {
  content: T[]
  totalPages: number
  totalElements: number
  page: number
  size: number
}

function mapReview(raw: BackendReviewResponse): Review {
  return {
    id: raw.id,
    albumId: raw.albumId,
    authorId: raw.authorId,
    score: raw.score,
    reviewText: raw.reviewText,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
    author: raw.authorUsername
      ? { id: raw.authorId, username: raw.authorUsername, avatarUrl: raw.authorImageUrl ?? undefined }
      : undefined,
  }
}

export class HttpReviewService implements IReviewService {
  async getByAlbum(albumId: string): Promise<Review[]> {
    const page = await get<BackendPage<BackendReviewResponse>>(
      `/api/v1/reviews?albumId=${albumId}&size=100&sort=createdAt,desc`
    )
    return page.content.map(mapReview)
  }

  async getRecent(limit: number): Promise<Review[]> {
    const page = await get<BackendPage<BackendReviewResponse>>(
      `/api/v1/reviews?size=${limit}&sort=createdAt,desc`
    )
    return page.content.map(mapReview)
  }

  async getByUser(userId: string): Promise<Review[]> {
    const page = await get<BackendPage<BackendReviewResponse>>(
      `/api/v1/reviews?authorId=${userId}&size=100&sort=createdAt,desc`
    )
    return page.content.map(mapReview)
  }

  async create(albumId: string, dto: CreateReviewDto): Promise<Review> {
    const raw = await post<BackendReviewResponse>('/api/v1/reviews', {
      albumId,
      score: dto.score,
      reviewText: dto.reviewText,
    })
    return mapReview(raw)
  }

  async update(id: string, dto: UpdateReviewDto): Promise<Review> {
    const patches: Promise<unknown>[] = []
    if (dto.score !== undefined)      patches.push(patch(`/api/v1/reviews/${id}/score`,       { score: dto.score }))
    if (dto.reviewText !== undefined) patches.push(patch(`/api/v1/reviews/${id}/review-text`, { reviewText: dto.reviewText }))
    await Promise.all(patches)
    const raw = await get<BackendReviewResponse>(`/api/v1/reviews/${id}`)
    return mapReview(raw)
  }

  delete(id: string): Promise<void> {
    return del(`/api/v1/reviews/${id}`)
  }
}