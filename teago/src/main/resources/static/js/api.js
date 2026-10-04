// All requests use the Spring Boot origin and its session cookie.
const API = '';
const TeaGo = (() => {
  let csrfRequest;
  const sessionKeys = ['userId','role','fullName','username','landOwnerId','landSize','factoryOwnerId','factoryName','factoryLocation'];
  function cacheSession(data) {
    sessionKeys.forEach(key => localStorage.setItem('teago_' + key, data[key] ?? ''));
  }
  async function csrf() {
    if (!csrfRequest) csrfRequest = window.fetch('/api/auth/csrf', {credentials:'same-origin'})
      .then(async response => {
        if (!response.ok) throw new Error('Could not establish a secure session');
        return response.json();
      }).catch(error => { csrfRequest = null; throw error; });
    return csrfRequest;
  }
  async function request(url, options = {}) {
    const headers = new Headers(options.headers || {});
    const method = (options.method || 'GET').toUpperCase();
    if (!['GET','HEAD','OPTIONS'].includes(method)) {
      const token = await csrf();
      headers.set(token.headerName, token.token);
    }
    const response = await window.fetch(url, {...options, method, headers, credentials:'same-origin'});
    if (url.endsWith('/api/auth/login') || url.endsWith('/api/auth/logout') || response.status === 403) csrfRequest = null;
    if (response.status === 401 && !url.includes('/api/auth/') && location.pathname !== '/index.html' && location.pathname !== '/') {
      sessionKeys.forEach(key => localStorage.removeItem('teago_' + key));
      location.assign('/index.html');
    }
    return response;
  }
  async function json(url, options) {
    const response = await request(url, options);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || `Request failed (${response.status})`);
    return data;
  }
  async function hydrateSession(role) {
    try {
      const data = await json('/api/auth/session');
      if (data.role !== role) throw new Error('Incorrect role');
      cacheSession(data); return true;
    } catch (_) { location.assign('/index.html'); return false; }
  }
  async function logout() {
    await json('/api/auth/logout', {method:'POST'});
    sessionKeys.forEach(key => localStorage.removeItem('teago_' + key));
    localStorage.removeItem('teago_lastPage');
    location.assign('/index.html');
  }
  return {request,json,cacheSession,hydrateSession,logout};
})();
const cacheSession = TeaGo.cacheSession;
const hydrateSession = TeaGo.hydrateSession;
function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
}
