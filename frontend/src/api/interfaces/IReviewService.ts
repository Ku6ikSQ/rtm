import type { Review, CreateReviewDto, UpdateReviewDto } from '@/types/entities'

export interface IReviewService {
  getByAlbum(albumId: string): Promise<Review[]>
  getByUser(userId: string): Promise<Review[]>
  getRecent(limit: number): Promise<Review[]>
  create(albumId: string, dto: CreateReviewDto): Promise<Review>
  update(id: string, dto: UpdateReviewDto): Promise<Review>
  delete(id: string): Promise<void>
}
