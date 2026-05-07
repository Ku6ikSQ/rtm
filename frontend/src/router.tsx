import { createBrowserRouter } from 'react-router-dom'
import { RootLayout } from './components/common/RootLayout'
import { ProtectedRoute } from './components/common/ProtectedRoute'
import { GuestRoute } from './components/common/GuestRoute'
import { HomePage } from './pages/HomePage'
import { CatalogPage } from './pages/CatalogPage'
import { AlbumPage } from './pages/AlbumPage'
import { ArtistPage } from './pages/ArtistPage'
import { GenresPage } from './pages/GenresPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ProfilePage } from './pages/ProfilePage'
import { AdminPage } from './pages/AdminPage'
import { CreateAlbumPage } from './pages/CreateAlbumPage'
import { EditAlbumPage } from './pages/EditAlbumPage'
import { NotFoundPage } from './pages/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    errorElement: <NotFoundPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'catalog', element: <CatalogPage /> },
      { path: 'album/:id', element: <AlbumPage /> },
      { path: 'artist/:id', element: <ArtistPage /> },
      { path: 'genres', element: <GenresPage /> },
      {
        element: <GuestRoute />,
        children: [
          { path: 'login', element: <LoginPage /> },
          { path: 'register', element: <RegisterPage /> },
        ],
      },
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'profile', element: <ProfilePage /> },
        ],
      },
      {
        element: <ProtectedRoute roles={['ADMIN', 'MODERATOR']} />,
        children: [
          { path: 'admin', element: <AdminPage /> },
          { path: 'album/new', element: <CreateAlbumPage /> },
          { path: 'album/:id/edit', element: <EditAlbumPage /> },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
