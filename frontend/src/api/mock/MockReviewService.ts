import type { IReviewService } from '@/api/interfaces/IReviewService'
import type { Review, CreateReviewDto, UpdateReviewDto } from '@/types/entities'
import { ApiError } from '@/types/errors'
import { delay } from '@/utils/delay'
import { mockReviews, mockAlbums, mockUsers } from './data'

const SESSION_KEY = 'rtm-mock-uid'
function getCurrentUserId(): string | null {
  return sessionStorage.getItem(SESSION_KEY)
}

function recalcAlbumRating(albumId: string) {
  const reviews = mockReviews.filter((r) => r.albumId === albumId)
  const album = mockAlbums.find((a) => a.id === albumId)
  if (!album) return
  album.reviewCount = reviews.length
  album.avgRating =
    reviews.length > 0 ? reviews.reduce((sum, r) => sum + r.score, 0) / reviews.length : 0
}

export class MockReviewService implements IReviewService {
  async getByAlbum(albumId: string): Promise<Review[]> {
    await delay(250)
    return mockReviews
      .filter((r) => r.albumId === albumId)
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  }

  async getByUser(userId: string): Promise<Review[]> {
    await delay(250)
    return mockReviews
      .filter((r) => r.authorId === userId)
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  }

  async create(albumId: string, dto: CreateReviewDto): Promise<Review> {
    await delay(350)
    const userId = getCurrentUserId()
    if (!userId) throw new ApiError(401, 'Необходимо войти в аккаунт')

    const existing = mockReviews.find((r) => r.albumId === albumId && r.authorId === userId)
    if (existing) throw new ApiError(409, 'Вы уже написали рецензию на этот альбом')

    const author = mockUsers.find((u) => u.id === userId)
    const review: Review = {
      id: String(Date.now()),
      albumId,
      authorId: userId,
      score: dto.score,
      reviewText: dto.reviewText,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      author: author ? { id: author.id, username: author.username } : undefined,
    }
    mockReviews.push(review)
    recalcAlbumRating(albumId)
    return review
  }

  async update(id: string, dto: UpdateReviewDto): Promise<Review> {
    await delay(300)
    const idx = mockReviews.findIndex((r) => r.id === id)
    if (idx === -1) throw new ApiError(404, 'Рецензия не найдена')
    const now = new Date().toISOString()
    mockReviews[idx] = { ...mockReviews[idx], ...dto, updatedAt: now }
    recalcAlbumRating(mockReviews[idx].albumId)
    return mockReviews[idx]
  }

  async delete(id: string): Promise<void> {
    await delay(250)
    const idx = mockReviews.findIndex((r) => r.id === id)
    if (idx === -1) throw new ApiError(404, 'Рецензия не найдена')
    const albumId = mockReviews[idx].albumId
    mockReviews.splice(idx, 1)
    recalcAlbumRating(albumId)
  }
}
