export interface Movie {
  id: string
  title: string
  originalTitle?: string
  description?: string
  posterUrl?: string
  backdropUrl?: string
  releaseDate?: string
  durationMinutes?: number
  language?: string
  ageRating?: string
  imdbScore?: number
  genres?: string[]
  director?: string
  castList?: string
}

export interface Showtime {
  id: string
  movieId: string
  movieTitle: string
  cinemaId?: string
  cinemaName: string
  screenName: string
  startTime: string
  format: string
  availableSeats: number
}

export type SeatStatusValue = 'AVAILABLE' | 'HELD' | 'CONFIRMED'
export type SeatCategory = 'STANDARD' | 'VIP' | 'COUPLE'

export interface SeatStatus {
  seatId: string
  seatCode: string
  status: SeatStatusValue
  category: SeatCategory
  unitPrice: number
}

export type BookingStatus = 'CONFIRMED' | 'PENDING_PAYMENT' | 'PROCESSING' | 'CANCELLED' | 'FAILED'

export interface Booking {
  bookingId: string
  movieTitle?: string
  cinemaName?: string
  showtimeStart?: string
  seats?: string[]
  itemCount?: number
  status: BookingStatus
  totalAmount: number
  paymentUrl?: string
}

export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export type TicketStatus = 'ISSUED' | 'USED' | 'EXPIRED' | 'VOIDED'

export interface Ticket {
  id: string
  bookingId: string
  movieTitle: string
  cinemaName: string
  screenName: string
  seatCode: string
  showtimeStart: string
  serialNumber: string
  qrCodeBase64?: string
  status: TicketStatus
  unitPrice: number
}

export interface Cinema {
  id: string
  name: string
  address: string
  city: string
  phone?: string
}

export interface Screen {
  id: string
  name: string
  cinemaId: string
}

export interface UserProfile {
  displayName?: string
  phoneNumber?: string
  address?: string
  dateOfBirth?: string
  gender?: string
}

export interface Promotion {
  id: string
  cinemaId?: string
  movieId?: string
}

export interface KeycloakUserInfo {
  preferred_username: string
  name?: string
  email?: string
  sub?: string
}

export interface BookingPayload {
  showtimeId: string
  seatIds: string[]
  paymentMethod: string
  voucherCode?: string
}
