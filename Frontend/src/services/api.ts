import axios from 'axios'

function normalizeBaseUrl(url: string): string {
  return url.replace(/\/+$/, '')
}

const raw = import.meta.env.VITE_API_BASE_URL?.trim()
const baseURL = raw ? normalizeBaseUrl(raw) : ''

export const api = axios.create({
  baseURL: baseURL || undefined,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30_000,
})
