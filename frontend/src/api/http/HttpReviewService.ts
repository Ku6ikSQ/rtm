import type { IReviewService } from '@/api/interfaces/IReviewService'
import type { Review, CreateReviewDto, UpdateReviewDto } from '@/types/entities'
import { get, post, put, del } from './apiClient'

export class HttpReviewService implements IReviewService {
  getByAlbum(albumId: string): Promise<Review[]> {
    return get(`/api/v1/albums/${albumId}/reviews`)
  }
  getByUser(userId: string): Promise<Review[]> {
    return get(`/api/v1/users/${userId}/reviews`)
  }
  create(albumId: string, dto: CreateReviewDto): Promise<Review> {
    return post(`/api/v1/albums/${albumId}/reviews`, dto)
  }
  update(id: string, dto: UpdateReviewDto): Promise<Review> {
    return put(`/api/v1/reviews/${id}`, dto)
  }
  delete(id: string): Promise<void> {
    return del(`/api/v1/reviews/${id}`)
  }
}
