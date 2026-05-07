import type { IAuthService } from './interfaces/IAuthService'
import type { IAlbumService } from './interfaces/IAlbumService'
import type { IArtistService } from './interfaces/IArtistService'
import type { IGenreService } from './interfaces/IGenreService'
import type { IPlatformService } from './interfaces/IPlatformService'
import type { IReviewService } from './interfaces/IReviewService'
import type { ITrackService } from './interfaces/ITrackService'
import type { IUserService } from './interfaces/IUserService'

const useMock = import.meta.env.VITE_USE_MOCK === 'true'

function makeMock<M, H>(MockCtor: new () => M, HttpCtor: new () => H): M | H {
  return useMock ? new MockCtor() : new HttpCtor()
}

import { MockAuthService } from './mock/MockAuthService'
import { MockAlbumService } from './mock/MockAlbumService'
import { MockArtistService } from './mock/MockArtistService'
import { MockGenreService } from './mock/MockGenreService'
import { MockPlatformService } from './mock/MockPlatformService'
import { MockReviewService } from './mock/MockReviewService'
import { MockTrackService } from './mock/MockTrackService'
import { MockUserService } from './mock/MockUserService'

import { HttpAuthService } from './http/HttpAuthService'
import { HttpAlbumService } from './http/HttpAlbumService'
import { HttpArtistService } from './http/HttpArtistService'
import { HttpGenreService } from './http/HttpGenreService'
import { HttpPlatformService } from './http/HttpPlatformService'
import { HttpReviewService } from './http/HttpReviewService'
import { HttpTrackService } from './http/HttpTrackService'
import { HttpUserService } from './http/HttpUserService'

export const authService: IAuthService = makeMock(MockAuthService, HttpAuthService)
export const albumService: IAlbumService = makeMock(MockAlbumService, HttpAlbumService)
export const artistService: IArtistService = makeMock(MockArtistService, HttpArtistService)
export const genreService: IGenreService = makeMock(MockGenreService, HttpGenreService)
export const platformService: IPlatformService = makeMock(MockPlatformService, HttpPlatformService)
export const reviewService: IReviewService = makeMock(MockReviewService, HttpReviewService)
export const trackService: ITrackService = makeMock(MockTrackService, HttpTrackService)
export const userService: IUserService = makeMock(MockUserService, HttpUserService)
