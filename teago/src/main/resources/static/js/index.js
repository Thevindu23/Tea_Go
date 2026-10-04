

  (async function restoreSessionOnLanding() {
    try {
      const res = await TeaGo.request(`${API}/api/auth/session`, { credentials: 'include' });
      if (!res.ok) return;

      const data = await res.json();
      cacheSession(data);

      if (data.role === 'LAND_OWNER') {
        window.location.href = 'dashboard-owner.html';
      } else if (data.role === 'FACTORY_OWNER') {
        window.location.href = 'dashboard-factory.html';
      }
    } catch (e) {
      // If session check fails, user stays on login page.
    }
  })();

  // ── Tab switching ─────────────────────────────────────────────────────
  function setTab(el, formId) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    el.classList.add('active');
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'none';
    document.getElementById(formId).style.display = 'block';
  }

  function switchTab(formId) {
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(t => t.classList.remove('active'));
    tabs[formId === 'login-form' ? 0 : 1].classList.add('active');
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'none';
    document.getElementById(formId).style.display = 'block';
  }

  // ── Show / hide role-specific fields ─────────────────────────────────
  function onRoleChange() {
    const role = document.getElementById('reg-role').value;
    ['fields-common', 'fields-land-owner', 'fields-factory-owner', 'fields-submit']
      .forEach(id => document.getElementById(id).style.display = 'none');
    if (!role) return;
    document.getElementById('fields-common').style.display = 'block';
    if (role === 'land_owner')    document.getElementById('fields-land-owner').style.display    = 'block';
    if (role === 'factory_owner') document.getElementById('fields-factory-owner').style.display = 'block';
    document.getElementById('fields-submit').style.display = 'block';
  }

  // ── Toast ─────────────────────────────────────────────────────────────
  function showToast(msg, isError = false) {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.style.display = 'block';
    t.style.background = isError ? '#c0392b' : 'var(--tea-dark)';
    setTimeout(() => { t.style.display = 'none'; }, 3500);
  }

  // ── LOGIN ─────────────────────────────────────────────────────────────
  async function handleLogin() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const role     = document.getElementById('role').value;
    if (!username || !password || !role) {
      showToast('Please fill in all fields.', true); return;
    }

    const btn = document.querySelector('#login-form .form-btn');
    btn.disabled = true; btn.textContent = 'Signing in…';

    try {
      const res  = await TeaGo.request(`${API}/api/auth/login`, {
        method : 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body   : JSON.stringify({ username, password })
      });
      const data = await res.json();
      if (!res.ok) { showToast('❌ ' + (data.message || 'Login failed.'), true); return; }

      const roleMap = { land_owner: 'LAND_OWNER', factory_owner: 'FACTORY_OWNER' };
      if (data.role !== roleMap[role]) {
        showToast('❌ Wrong role selected — please choose the correct one.', true); return;
      }

      cacheSession(data);

      showToast(`✓ Welcome back, ${escapeHtml(data.fullName)}! Redirecting…`);
      setTimeout(() => {
        if      (data.role === 'LAND_OWNER')    window.location.href = 'dashboard-owner.html';
        else if (data.role === 'FACTORY_OWNER') window.location.href = 'dashboard-factory.html';
      }, 1200);

    } catch (e) {
      showToast('❌ Cannot reach server. Is Spring Boot running on port 8080?', true);
    } finally {
      btn.disabled = false; btn.textContent = 'Sign In →';
    }
  }

  // ── REGISTER ──────────────────────────────────────────────────────────
  async function handleRegister() {
    const role = document.getElementById('reg-role').value;

    // Common fields (shown for both roles)
    const nic      = document.getElementById('reg-nic').value.trim();
    const username = document.getElementById('reg-username').value.trim();
    const password = document.getElementById('reg-password').value.trim();
    const contact  = document.getElementById('reg-contact').value.trim();
    const address  = document.getElementById('reg-address').value.trim();

    if (!nic || !username || !password || !contact || !address) {
      showToast('Please fill in all account details.', true); return;
    }
    if (nic.length < 9) {
      showToast('Please enter a valid NIC number (min 9 chars).', true); return;
    }
    if (password.length < 8 || password.length > 72) {
      showToast('Password must be 8–72 characters.', true); return;
    }

    let body = { nic, username, password, contactNumber: contact, address, role };

    // ── Land Owner fields ─────────────────────────────────────────────
    if (role === 'land_owner') {
      const nameWithInitials             = document.getElementById('reg-name-initials').value.trim();
      const fullName                     = document.getElementById('reg-full-name').value.trim();
      const dateOfBirth                  = document.getElementById('reg-dob').value;
      const landLocation                 = document.getElementById('reg-land-location').value.trim();
      const landSize                     = document.getElementById('reg-land-size').value.trim();
      const bankAccountName              = document.getElementById('reg-bank-acc-name').value.trim();
      const bankName                     = document.getElementById('reg-bank-name').value.trim();
      const bankBranch                   = document.getElementById('reg-bank-branch').value.trim();
      const bankAccountNumber            = document.getElementById('reg-bank-acc-number').value.trim();
      const emergencyContactName         = document.getElementById('reg-em-name').value.trim();
      const emergencyContactAddress      = document.getElementById('reg-em-address').value.trim();
      const emergencyContactPhone        = document.getElementById('reg-em-phone').value.trim();
      const emergencyContactRelationship = document.getElementById('reg-em-relationship').value.trim();

      if (!nameWithInitials)             { showToast('Please enter your name with initials.', true); return; }
      if (!fullName)                     { showToast('Please enter your full name.', true); return; }
      if (!dateOfBirth)                  { showToast('Please enter your date of birth.', true); return; }
      if (!landLocation)                 { showToast('Please enter your land location.', true); return; }
      if (!landSize)                     { showToast('Please enter your land size.', true); return; }
      if (!bankAccountName)              { showToast('Please enter your bank account name.', true); return; }
      if (!bankName)                     { showToast('Please enter your bank name.', true); return; }
      if (!bankBranch)                   { showToast('Please enter your bank branch.', true); return; }
      if (!bankAccountNumber)            { showToast('Please enter your bank account number.', true); return; }
      if (!emergencyContactName)         { showToast('Please enter emergency contact name.', true); return; }
      if (!emergencyContactAddress)      { showToast('Please enter emergency contact address.', true); return; }
      if (!emergencyContactPhone)        { showToast('Please enter emergency contact phone.', true); return; }
      if (!emergencyContactRelationship) { showToast('Please enter relationship with emergency contact.', true); return; }

      body.name = fullName;
      Object.assign(body, {
        nameWithInitials, fullName, dateOfBirth,
        landLocation, landSize: parseFloat(landSize),
        bankAccountName, bankName, bankBranch, bankAccountNumber,
        emergencyContactName, emergencyContactAddress,
        emergencyContactPhone, emergencyContactRelationship
      });
    }

    // ── Factory Owner fields ──────────────────────────────────────────
    if (role === 'factory_owner') {
      const foName             = document.getElementById('reg-fo-name').value.trim();
      const factoryName        = document.getElementById('reg-factory-name').value.trim();
      const factoryLocation    = document.getElementById('reg-factory-location').value.trim();
      const registrationNumber = document.getElementById('reg-reg-number').value.trim();

      if (!foName)             { showToast('Please enter your full name.', true); return; }
      if (!factoryName)        { showToast('Please enter your factory name.', true); return; }
      if (!factoryLocation)    { showToast('Please enter your factory location.', true); return; }
      if (!registrationNumber) { showToast('Please enter your business registration number.', true); return; }

      body.name = foName;
      Object.assign(body, { factoryOwnerFullName: foName, factoryName, factoryLocation, registrationNumber });
    }

    const btn = document.getElementById('reg-btn');
    btn.disabled = true; btn.textContent = 'Creating account…';

    try {
      const res  = await TeaGo.request(`${API}/api/auth/register`, {
        method : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body   : JSON.stringify(body)
      });
      const data = await res.json();
      if (!res.ok) { showToast('❌ ' + (data.message || 'Registration failed.'), true); return; }

      showToast(`✓ Account created! Welcome to TeaGo, ${escapeHtml(body.name)}. Please sign in.`);

      // Clear all fields
      ['reg-nic', 'reg-username', 'reg-password', 'reg-contact', 'reg-address',
       'reg-name-initials', 'reg-full-name', 'reg-dob',
       'reg-land-location', 'reg-land-size',
       'reg-bank-acc-name', 'reg-bank-name', 'reg-bank-branch', 'reg-bank-acc-number',
       'reg-em-name', 'reg-em-address', 'reg-em-phone', 'reg-em-relationship',
       'reg-fo-name', 'reg-factory-name', 'reg-factory-location', 'reg-reg-number']
        .forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
      document.getElementById('reg-role').value = '';
      ['fields-common', 'fields-land-owner', 'fields-factory-owner', 'fields-submit']
        .forEach(id => document.getElementById(id).style.display = 'none');

      setTimeout(() => switchTab('login-form'), 2000);

    } catch (e) {
      showToast('❌ Cannot reach server. Is Spring Boot running on port 8080?', true);
    } finally {
      btn.disabled = false; btn.textContent = 'Create Account →';
    }
  }

  // ── Scroll reveal ─────────────────────────────────────────────────────
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(e => {
      if (e.isIntersecting) {
        e.target.style.animation = 'fadeUp 0.6s ease both';
        observer.unobserve(e.target);
      }
    });
  }, { threshold: 0.1 });

  document.querySelectorAll('.feat-card, .role-card, .step').forEach(el => {
    el.style.opacity = '0';
    observer.observe(el);
  });
