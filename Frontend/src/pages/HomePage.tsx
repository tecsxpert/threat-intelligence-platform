import { api } from '../services/api'

export function HomePage() {
  const configured = Boolean(import.meta.env.VITE_API_BASE_URL?.trim())
  const resolved = api.defaults.baseURL ?? null

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <p className="text-sm font-medium uppercase tracking-wide text-violet-400">
          Threat Intelligence Platform
        </p>
        <h1 className="text-3xl font-semibold tracking-tight text-white sm:text-4xl">
          Frontend
        </h1>
        <p className="text-slate-400">
          React, Vite, Axios, and Tailwind are configured. Point{' '}
          <code className="rounded bg-slate-800 px-1.5 py-0.5 text-sm text-violet-200">
            VITE_API_BASE_URL
          </code>{' '}
          at your API (see <code className="text-sm">.env.example</code>).
        </p>
      </header>

      <dl className="grid gap-3 rounded-xl border border-slate-800 bg-slate-900/60 p-4 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-slate-500">Env set</dt>
          <dd className="font-mono text-slate-200">
            {configured ? 'yes' : 'no (optional)'}
          </dd>
        </div>
        <div>
          <dt className="text-slate-500">Axios baseURL</dt>
          <dd className="break-all font-mono text-slate-200">
            {resolved ?? 'undefined — use relative / same-origin requests'}
          </dd>
        </div>
      </dl>
    </div>
  )
}
