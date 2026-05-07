import { Outlet } from 'react-router-dom'
import { Header } from './Header'
import { Footer } from './Footer'
import { BottomNav } from './BottomNav'

export function RootLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 pb-16 pt-4 md:pb-4">
        <Outlet />
      </main>
      <Footer />
      <BottomNav />
    </div>
  )
}
