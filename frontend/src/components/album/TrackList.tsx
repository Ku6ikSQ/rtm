import { formatDuration } from '@/utils/formatters'
import type { Track } from '@/types/entities'

interface TrackListProps {
  tracks: Track[]
}

export function TrackList({ tracks }: TrackListProps) {
  if (tracks.length === 0) return null

  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-border text-left text-xs uppercase tracking-wider text-muted-foreground">
          <th className="pb-2 pr-4 font-medium">#</th>
          <th className="pb-2 font-medium">Название</th>
          <th className="pb-2 pl-4 text-right font-medium">Длина</th>
        </tr>
      </thead>
      <tbody>
        {tracks.map((track) => (
          <tr key={track.id} className="border-b border-border last:border-0">
            <td className="py-2 pr-4 text-muted-foreground">{track.trackNumber}</td>
            <td className="py-2">{track.title}</td>
            <td className="py-2 pl-4 text-right tabular-nums text-muted-foreground">
              {formatDuration(track.durationSeconds)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
