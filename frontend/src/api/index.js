const BASE = ''

async function request(url, options = {}) {
  const res = await fetch(BASE + url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw new Error(body.error || `HTTP ${res.status}`)
  }
  if (res.status === 204) return null
  return res.json()
}

export const api = {
  listDatabases:    ()              => request('/api/databases'),
  getCurrentDb:     ()              => request('/api/databases/current'),
  switchDatabase:   (name)          => request('/api/databases/current',    { method: 'PUT',  body: JSON.stringify({ database: name }) }),
  createDatabase:  (name)          => request('/api/databases',              { method: 'POST',   body: JSON.stringify({ database: name }) }),
  deleteDatabase:  (name)          => request(`/api/databases/${name}`,       { method: 'DELETE' }),
  listTables:       ()              => request('/api/tables'),
  getTable:         (name)          => request(`/api/tables/${name}`),
  createTable:      (data)          => request('/api/tables',              { method: 'POST', body: JSON.stringify(data) }),
  deleteTable:      (name)          => request(`/api/tables/${name}`,      { method: 'DELETE' }),
  alterTable:       (name, data)    => request(`/api/tables/${name}`,      { method: 'PUT',  body: JSON.stringify(data) }),
  getRows:          (tableName)     => request(`/api/tables/${tableName}/rows`),
  insertRow:        (tableName, data) => request(`/api/tables/${tableName}/rows`,      { method: 'POST', body: JSON.stringify(data) }),
  updateRow:        (tableName, id, data) => request(`/api/tables/${tableName}/rows/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteRow:        (tableName, id) => request(`/api/tables/${tableName}/rows/${id}`, { method: 'DELETE' }),
}
