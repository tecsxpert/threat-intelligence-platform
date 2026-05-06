import { useEffect, useState } from 'react'
import { testAI } from './services/api'
import { AppShell } from './components/AppShell'
import { HomePage } from './pages/HomePage'

export default function App() {

  const [response, setResponse] = useState<any>(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await testAI()
        setResponse(data)
      } catch (err) {
        console.error(err)
      }
    }

    fetchData()
  }, [])

  return (
    <AppShell>
      <HomePage />

      <div style={{ padding: "20px" }}>
        <h2>AI Output</h2>

        {response ? (
          <p style={{ whiteSpace: "pre-line" }}>
            {response?.data?.response}
          </p>
        ) : (
          <p>Loading...</p>
        )}
      </div>
    </AppShell>
  )
}