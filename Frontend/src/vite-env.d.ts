/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Backend API root (no trailing slash), e.g. http://localhost:8080/api */
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
