

document.addEventListener('DOMContentLoaded', async () => {
  const sessionOk = await hydrateSession('LAND_OWNER');
  if(!sessionOk) return;

  const d = new Date();
  document.getElementById('date-chip').textContent =
      d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  document.getElementById('month-lbl').textContent =
      d.toLocaleDateString('en-GB', { month: 'long', year: 'numeric' });
  document.getElementById('l-date').valueAsDate = new Date();

  
  const fullName    = localStorage.getItem('teago_fullName')    || 'Land Owner';
  const landOwnerId = localStorage.getItem('teago_landOwnerId') || '';
  const landSize    = localStorage.getItem('teago_landSize')    || '';
  const role        = localStorage.getItem('teago_role')        || '';

  
  if (!role || role !== 'LAND_OWNER') {
    window.location.href = 'index.html';
    return;
  }

  
  const initials = fullName.trim().split(/\s+/)
      .map(w => w[0] ? w[0].toUpperCase() : '').slice(0, 2).join('');
  document.getElementById('prof-av').textContent   = initials || '?';
  document.getElementById('prof-name').textContent = fullName;
  const idPart   = landOwnerId ? `LO-${String(landOwnerId).padStart(3, '0')}` : '—';
  const sizePart = landSize    ? `${landSize} acres` : '—';
  document.getElementById('prof-id').textContent   = `ID: ${idPart} · ${sizePart}`;

  
  document.getElementById('greeting-name').textContent = fullName.split(' ')[0];

  loadDashboardOverview();
  window.setInterval(loadDashboardOverview, 60000);
  loadLiveRate();            
  loadMyDeliveries();
  loadMyPayments();         
  loadLoans();               
  loadMyCheques();           
  loadFertilizerRequests();
  loadEmployees();
  loadTeaCollections();      
  loadWageSheet();           
  loadTeaCollectionSheet();   
  prepareComplaintFactoryOptions();
  loadMyComplaints();
  initBillControls();
  loadBillHistory();
  initIncomeStatementControls();

  
  const now = new Date();
  document.getElementById('wage-month').value = now.getMonth() + 1;
  document.getElementById('wage-year').value = now.getFullYear();
  document.getElementById('tea-month').value = now.getMonth() + 1;
  document.getElementById('tea-year').value = now.getFullYear();

  
  const lastPage = localStorage.getItem('teago_lastPage') || 'dash';
  if (lastPage && lastPage !== 'dash') {
    const navItems = document.querySelectorAll('.nav-item');
    let targetNavItem = null;
    navItems.forEach(item => {
      if (item.getAttribute('onclick') && item.getAttribute('onclick').includes(`'${lastPage}'`)) {
        targetNavItem = item;
      }
    });
    baseNav(lastPage, targetNavItem);
  }
});

const pages={dash:'My Dashboard',deliveries:'Tea Deliveries',payments:'My Payments',fertilizer:'Fertilizer Requests',loans:'Loans & Advances',cheques:'My Cheques',factories:'My Factories',employees:'My Employees',reports:'Reports',complaints:'Complaints'};

function baseNav(id,el){
  document.querySelectorAll('.page').forEach(p=>p.classList.remove('active'));
  document.getElementById('p-'+id).classList.add('active');
  document.getElementById('tb-title').textContent=pages[id]||id;
  if(el){document.querySelectorAll('.nav-item').forEach(n=>n.classList.remove('active'));el.classList.add('active');}
  // Save the current page to localStorage for session persistence
  localStorage.setItem('teago_lastPage', id);
  // Refresh live rate from TeaRate table whenever deliveries page is opened
  if(id === 'deliveries') loadLiveRate();
  if(id === 'cheques') loadMyCheques();
}

function openM(id){document.getElementById(id).classList.add('on');}
function closeM(id){document.getElementById(id).classList.remove('on');}
document.querySelectorAll('.mo').forEach(m=>m.addEventListener('click',e=>{if(e.target===m)m.classList.remove('on');}));

let fertFactoryOptionsLoaded = false;
let loanFactoryOptionsLoaded = false;
let complaintFactoryOptionsLoaded = false;

async function prepareFertilizerFactoryOptions(){
  const sel = document.getElementById('f-factory');
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    sel.innerHTML = '<option value="">Sign in again to load factories</option>';
    fertFactoryOptionsLoaded = false;
    return;
  }

  sel.innerHTML = '<option value="">Loading your factories...</option>';

  try {
    const res = await TeaGo.request(`${API}/api/factories/registrations/${landOwnerId}`);
    if(!res.ok){
      sel.innerHTML = '<option value="">Could not load factories</option>';
      fertFactoryOptionsLoaded = false;
      return;
    }

    const regs = await res.json();
    const activeRegs = Array.isArray(regs) ? regs.filter(r => r.status === 'Active') : [];

    if(activeRegs.length === 0){
      sel.innerHTML = '<option value="">No approved factory found</option>';
      fertFactoryOptionsLoaded = false;
      return;
    }

    sel.innerHTML = '<option value="">-- Select Factory Owner --</option>';
    activeRegs.forEach(r => {
      const opt = document.createElement('option');
      opt.value = r.factoryId;
      opt.textContent = `${escapeHtml(r.factoryName)} (${escapeHtml(r.factoryLocation || 'Location N/A')})`;
      sel.appendChild(opt);
    });

    fertFactoryOptionsLoaded = true;
  } catch(e){
    sel.innerHTML = '<option value="">Cannot reach server</option>';
    fertFactoryOptionsLoaded = false;
    console.warn('prepareFertilizerFactoryOptions:', e.message);
  }
}

async function prepareLoanFactoryOptions(){
  const sel = document.getElementById('l-factory');
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    sel.innerHTML = '<option value="">Sign in again to load factories</option>';
    loanFactoryOptionsLoaded = false;
    return;
  }

  sel.innerHTML = '<option value="">Loading your factories...</option>';

  try {
    const res = await TeaGo.request(`${API}/api/factories/registrations/${landOwnerId}`);
    if(!res.ok){
      sel.innerHTML = '<option value="">Could not load factories</option>';
      loanFactoryOptionsLoaded = false;
      return;
    }

    const regs = await res.json();
    const activeRegs = Array.isArray(regs) ? regs.filter(r => r.status === 'Active') : [];

    if(activeRegs.length === 0){
      sel.innerHTML = '<option value="">No approved factory found</option>';
      loanFactoryOptionsLoaded = false;
      return;
    }

    sel.innerHTML = '<option value="">-- Select Factory Owner --</option>';
    activeRegs.forEach(r => {
      const opt = document.createElement('option');
      opt.value = r.factoryId;
      opt.textContent = `${escapeHtml(r.factoryName)} (${escapeHtml(r.factoryLocation || 'Location N/A')})`;
      sel.appendChild(opt);
    });

    loanFactoryOptionsLoaded = true;
  } catch(e){
    sel.innerHTML = '<option value="">Cannot reach server</option>';
    loanFactoryOptionsLoaded = false;
    console.warn('prepareLoanFactoryOptions:', e.message);
  }
}

async function prepareComplaintFactoryOptions(){
  const sel = document.getElementById('c-factory');
  if(!sel) return;

  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    sel.innerHTML = '<option value="">Sign in again to load factories</option>';
    complaintFactoryOptionsLoaded = false;
    return;
  }

  sel.innerHTML = '<option value="">Loading your factories...</option>';

  try {
    const res = await TeaGo.request(`${API}/api/factories/registrations/${landOwnerId}`);
    if(!res.ok){
      sel.innerHTML = '<option value="">Could not load factories</option>';
      complaintFactoryOptionsLoaded = false;
      return;
    }

    const regs = await res.json();
    const activeRegs = Array.isArray(regs) ? regs.filter(r => r.status === 'Active') : [];

    if(activeRegs.length === 0){
      sel.innerHTML = '<option value="">No approved factory found</option>';
      complaintFactoryOptionsLoaded = false;
      return;
    }

    sel.innerHTML = '<option value="">-- Select Factory Owner --</option>';
    activeRegs.forEach(r => {
      const opt = document.createElement('option');
      opt.value = r.factoryId;
      opt.textContent = `${escapeHtml(r.factoryName)} (${escapeHtml(r.factoryLocation || 'Location N/A')})`;
      sel.appendChild(opt);
    });

    complaintFactoryOptionsLoaded = true;
  } catch(e){
    sel.innerHTML = '<option value="">Cannot reach server</option>';
    complaintFactoryOptionsLoaded = false;
    console.warn('prepareComplaintFactoryOptions:', e.message);
  }
}

async function loadLoans(){
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);
  
  if(!Number.isFinite(landOwnerId)){
    document.getElementById('loan-tbody').innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--muted);">⚠️ Cannot load loans</td></tr>';
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/loans/owner/${landOwnerId}`);
    const loans = await res.json();
    
    if(!Array.isArray(loans) || loans.length === 0){
      document.getElementById('loan-tbody').innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--muted);padding:1.5rem;">No loans or advances yet</td></tr>';
      updateLoanStats([], landOwnerId);
      return;
    }

    
    loans.sort((a, b) => new Date(b.requestDate) - new Date(a.requestDate));

   
    const rows = loans.map(loan => {
      const dateStr = new Date(loan.requestDate).toLocaleDateString('en-GB', { year:'numeric', month:'2-digit', day:'2-digit' });
      const typeLabel = loan.requestType === 'ADVANCE' ? 'Advance' : 'Loan';
      const typeColor = loan.requestType === 'ADVANCE' ? 'gold' : 'sage';
      const amountStr = `Rs ${parseInt(loan.loanAmount).toLocaleString()}`;
      const statusBadge = getStatusBadge(loan.approvalStatus);
      return `<tr><td>${dateStr}</td><td><span class="b ${typeColor}">${typeLabel}</span></td><td>${amountStr}</td><td>${escapeHtml(loan.reason || '—')}</td><td>${statusBadge}</td></tr>`;
    }).join('');

    document.getElementById('loan-tbody').innerHTML = rows;
    updateLoanStats(loans, landOwnerId);
  } catch(e){
    document.getElementById('loan-tbody').innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--danger);">⚠️ Cannot reach server</td></tr>';
    console.warn('loadLoans:', e.message);
  }
}

function getStatusBadge(status){
  if(status === 'Pending') return '<span class="b y">Pending</span>';
  if(status === 'Approved') return '<span class="b g">✓ Approved</span>';
  if(status === 'Rejected') return '<span class="b r">✗ Rejected</span>';
  return '<span class="b">' + status + '</span>';
}

function updateLoanStats(loans, landOwnerId){
 
  const approved = loans.filter(l => l.approvalStatus === 'Approved');
  const pending = loans.filter(l => l.approvalStatus === 'Pending');

  const totalBorrowed = loans.reduce((sum, l) => sum + parseInt(l.loanAmount), 0);
  const approvedAmount = approved.reduce((sum, l) => sum + parseInt(l.loanAmount), 0);
  

  const monthlyDeduction = Math.min(approvedAmount, 10000); 
  const outstanding = Math.max(0, approvedAmount - monthlyDeduction);

 
  const formatCurrency = (val) => {
    if(val >= 1000) return `Rs ${(val / 1000).toFixed(0)}k`;
    return `Rs ${val}`;
  };

  document.getElementById('loan-stat-outstanding').innerHTML = `<div class="lbl">Outstanding</div><div class="val">${formatCurrency(outstanding)}</div>`;
  document.getElementById('loan-stat-monthly').innerHTML = `<div class="lbl">Monthly Deduction</div><div class="val">${formatCurrency(monthlyDeduction)}</div>`;
  document.getElementById('loan-stat-total').innerHTML = `<div class="lbl">Total Borrowed</div><div class="val">${formatCurrency(totalBorrowed)}</div>`;
  document.getElementById('loan-stat-pending').innerHTML = `<div class="lbl">Pending</div><div class="val">${pending.length}</div><div class="sub">Awaiting approval</div>`;
}

async function saveLoan(){
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);
  
  const amt = document.getElementById('l-amt').value;
  const reason = document.getElementById('l-reason').value;
  const requestType = document.getElementById('l-type').value;

  const factorySel = document.getElementById('l-factory');
  const factoryId = factorySel.value;

  if(!Number.isFinite(landOwnerId)){
    toast('Please sign in again to submit loan request.', true);
    return;
  }

  if(!loanFactoryOptionsLoaded || !factoryId){
    toast('Please select a factory owner.', true);
    return;
  }

  if(!amt || !reason){
    toast('Please fill all required fields.', true);
    return;
  }

  try {
    const response = await TeaGo.request(`${API}/api/loans`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        landOwnerId: landOwnerId,
        factoryOwnerId: parseInt(factoryId),
        loanAmount: parseInt(amt),
        reason: reason,
        requestType: requestType
      })
    });
    if(!response.ok){
      const errData = await response.json();
      toast('Error: ' + (errData.message || 'Failed to submit application'), true);
      return;
    }
    const typeLabel = requestType === 'ADVANCE' ? 'Advance' : 'Loan';
    toast('✓ ' + typeLabel + ' application submitted — awaiting factory approval');
    closeM('m-loan');
    document.getElementById('l-amt').value = '';
    document.getElementById('l-reason').value = '';
    document.getElementById('l-type').value = 'LOAN';
    loadLoans(); // Refresh the loan list
    loadDashboardOverview();
  } catch(e){
    toast('Cannot reach server. Please try again.', true);
    console.error('saveLoan:', e.message);
  }
}

async function saveFert(){
  const factorySel = document.getElementById('f-factory');
  const factoryId = factorySel.value;
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);
  const type=document.getElementById('f-type').value;
  const qty=document.getElementById('f-qty').value;
  const notes=document.getElementById('f-notes').value;

  if(!Number.isFinite(landOwnerId)){toast('Please sign in again to submit request.',true);return;}
  if(!fertFactoryOptionsLoaded || !factoryId){toast('Please select a factory owner.',true);return;}
  if(!qty){toast('Please enter quantity.',true);return;}

  const factoryLabel = factorySel.options[factorySel.selectedIndex].text;
  const btn = document.querySelector('#m-fert .btn.btn-p');
  const oldText = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Submitting...';

  try {
    const res = await TeaGo.request(`${API}/api/fertilizer`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        landOwnerId: landOwnerId,
        factoryOwnerId: parseInt(factoryId, 10),
        fertilizerType: type,
        quantity: parseFloat(qty)
      })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to submit fertilizer request'), true);
      return;
    }

    closeM('m-fert');
    await loadFertilizerRequests();
    loadDashboardOverview();
    toast('✓ Fertilizer request submitted to ' + factoryLabel);
    document.getElementById('f-qty').value='';
    document.getElementById('f-notes').value='';
  } catch(e){
    toast('❌ Network error — is the server running?', true);
    console.warn('saveFert:', e.message);
  } finally {
    btn.disabled = false;
    btn.textContent = oldText;
  }
}

function updateFertilizerStats(requests){
  const monthEl = document.getElementById('fert-stat-month');
  const pendingEl = document.getElementById('fert-stat-pending');
  const deductedEl = document.getElementById('fert-stat-deducted');
  if(!monthEl || !pendingEl || !deductedEl) return;

  const totalQty = requests.reduce((sum, r) => sum + (parseFloat(r.quantity) || 0), 0);
  const pendingCount = requests.filter(r => r.approvalStatus === 'Pending').length;
  const deducted = requests
    .filter(r => r.approvalStatus === 'Approved')
    .reduce((sum, r) => sum + (parseFloat(r.cost) || 0), 0);

  monthEl.textContent = `${Number(totalQty.toFixed(2)).toLocaleString()} kg`;
  pendingEl.textContent = String(pendingCount);
  deductedEl.textContent = `Rs ${Math.round(deducted).toLocaleString()}`;
}

// ── GET /api/fertilizer/owner/{id} — load fertilizer requests + stats ─────
async function loadFertilizerRequests(){
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);
  const tbody = document.getElementById('fert-tbody');
  if(!tbody) return;

  if(!Number.isFinite(landOwnerId)){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">Please sign in again to view fertilizer requests.</td></tr>';
    updateFertilizerStats([]);
    return;
  }

  tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">⏳ Loading...</td></tr>';

  try {
    const res = await TeaGo.request(`${API}/api/fertilizer/owner/${landOwnerId}`);
    if(!res.ok){
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--danger);padding:1.5rem;">❌ Could not load requests (${res.status})</td></tr>`;
      updateFertilizerStats([]);
      return;
    }

    const rows = await res.json();
    if(!Array.isArray(rows) || rows.length === 0){
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">No fertilizer requests yet.</td></tr>';
      updateFertilizerStats([]);
      return;
    }

    updateFertilizerStats(rows);

    tbody.innerHTML = rows
      .slice()
      .sort((a,b) => (b.requestId || 0) - (a.requestId || 0))
      .map(r => {
        const qty = parseFloat(r.quantity) || 0;
        const cost = parseFloat(r.cost) || 0;
        const unitPrice = qty > 0 ? (cost / qty) : 0;
        const statusClass = r.approvalStatus === 'Approved' ? 'g' : (r.approvalStatus === 'Rejected' ? 'r' : 'y');
        const reqDate = r.requestDate || r.requestedDate || '—';
        return `<tr>
          <td>${reqDate}</td>
          <td>${r.fertilizerType || '—'}</td>
          <td>${qty.toLocaleString()}</td>
          <td>Rs ${Math.round(unitPrice).toLocaleString()}</td>
          <td>Rs ${Math.round(cost).toLocaleString()}</td>
          <td><span class="b ${statusClass}">${r.approvalStatus || 'Pending'}</span></td>
        </tr>`;
      }).join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Cannot reach server.</td></tr>';
    updateFertilizerStats([]);
    console.warn('loadFertilizerRequests:', e.message);
  }
}

function updateEmployeeStats(employees){
  const totalEl = document.getElementById('emp-stat-total');
  if(!totalEl) return;

  const totalWorkers = employees.length;
  totalEl.textContent = totalWorkers.toLocaleString();
}

async function loadEmployees(){
  const tbody = document.getElementById('emp-tbody');
  if(!tbody) return;

  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--muted);padding:1.5rem;">Please sign in again to load employees.</td></tr>';
    updateEmployeeStats([]);
    return;
  }

  tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--muted);padding:1.5rem;">⏳ Loading employees...</td></tr>';

  try {
    const res = await TeaGo.request(`${API}/api/employees/owner/${landOwnerId}`);
    if(!res.ok){
      tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;color:var(--danger);padding:1.5rem;">❌ Could not load employees (${res.status})</td></tr>`;
      updateEmployeeStats([]);
      return;
    }

    const employees = await res.json();
    if(!Array.isArray(employees) || employees.length === 0){
      tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--muted);padding:1.5rem;">No employees added yet.</td></tr>';
      updateEmployeeStats([]);
      return;
    }

    updateEmployeeStats(employees);
    tbody.innerHTML = employees
      .slice()
      .sort((a,b) => (a.employeeId || 0) - (b.employeeId || 0))
      .map(e => {
        const id = e.employeeId ? `EMP-${String(e.employeeId).padStart(3, '0')}` : '—';
        const name = e.employeeName || '—';
        const nic = '—';
        const contact = e.employeePhone || '—';
        return `<tr>
          <td>${id}</td>
          <td>${escapeHtml(name)}</td>
          <td>${escapeHtml(nic)}</td>
          <td>${contact}</td>
        </tr>`;
      }).join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Cannot reach server.</td></tr>';
    updateEmployeeStats([]);
    console.warn('loadEmployees:', e.message);
  }
}

async function saveEmp(){
  const name=document.getElementById('e-name').value;
  const nic=document.getElementById('e-nic').value;
  const phone=document.getElementById('e-tel').value;
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);
  if(!Number.isFinite(landOwnerId)){toast('Please sign in again to add employee.',true);return;}
  if(!name||!nic||!phone){toast('Please fill all required fields.',true);return;}

  const btn = document.querySelector('#m-emp .btn.btn-p');
  const oldText = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Saving...';

  try {
    const res = await TeaGo.request(`${API}/api/employees`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        landOwnerId: landOwnerId,
        employeeName: name,
        employeeNic: nic,
        employeePhone: phone
      })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to add employee'), true);
      return;
    }

    closeM('m-emp');
    await loadEmployees();
    loadDashboardOverview();
    toast('✓ Employee '+(data.employeeName || name)+' added');
  } catch(e){
    toast('❌ Network error — is the server running?', true);
    console.warn('saveEmp:', e.message);
  } finally {
    btn.disabled = false;
    btn.textContent = oldText;
  }

  ['e-name', 'e-nic', 'e-tel'].forEach(id => document.getElementById(id).value = '');
}

async function verifyCheq(chequeId){
  try {
    const res = await TeaGo.request(`${API}/api/cheques/${chequeId}/verification-status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'Verified' })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to verify cheque'), true);
      return;
    }

    toast('✓ Cheque verified — confirmed as received');
    await loadMyCheques();
    loadDashboardOverview();
  } catch(e){
    toast('❌ Network error while verifying cheque', true);
    console.warn('verifyCheq:', e.message);
  }
}

async function markChequeDelivered(chequeId){
  try {
    const res = await TeaGo.request(`${API}/api/cheques/${chequeId}/delivery-status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'Delivered' })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to mark cheque as delivered'), true);
      return;
    }

    toast('✓ Cheque marked as delivered — awaiting verification');
    await loadMyCheques();
    loadDashboardOverview();
  } catch(e){
    toast('❌ Network error while marking cheque as delivered', true);
    console.warn('markChequeDelivered:', e.message);
  }
}

function ownerChequeStatusBadge(cheque){
  const delivery = cheque.deliveryStatus;
  const verification = cheque.verificationStatus;
  const issueDate = cheque.issueDate;

  if(verification === 'Verified') return '<span class="b g">✓ Verified</span>';
  if(delivery === 'Delivered' && verification === 'Pending') return '<span class="b bl">Pending Verify</span>';
  if(issueDate && delivery === 'In_Transit') return '<span class="b sage">Issued</span>'; // Cheque issued (has issueDate)
  if(delivery === 'Pending' && !issueDate) return '<span class="b y">Approved</span>'; // Awaiting issue
  if(delivery === 'In_Transit') return '<span class="b g">In Transit</span>';
  if(delivery === 'Returned') return '<span class="b r">Returned</span>';
  return `<span class="b">${delivery || 'Pending'}</span>`;
}

let ownerBillCache = [];
let ownerMonthlyBillCache = null;
let ownerIncomeStatementCache = null;

function formatCurrency(value){
  const n = Number(value || 0);
  return 'Rs ' + n.toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
}

async function loadMyCheques(){
  const tbody = document.getElementById('cheq-tbody');
  if(!tbody) return;

  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">Please sign in again.</td></tr>';
    return;
  }

  tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading cheques...</td></tr>';

  try {
    const res = await TeaGo.request(`${API}/api/cheques/owner/${landOwnerId}`);
    const raw = res.ok ? await res.json() : [];
    const cheques = Array.isArray(raw) ? raw : [];

    const sorted = cheques.slice().sort((a,b) => (b.chequeId || 0) - (a.chequeId || 0));

    const total = sorted.length;
    const inTransit = sorted.filter(c => c.deliveryStatus === 'Pending').length;
    const toVerify = sorted.filter(c => c.deliveryStatus === 'Delivered' && c.verificationStatus === 'Pending').length;
    const verified = sorted.filter(c => c.verificationStatus === 'Verified').length;

    const statTotal = document.getElementById('cheq-stat-total');
    const statTransit = document.getElementById('cheq-stat-transit');
    const statVerify = document.getElementById('cheq-stat-verify');
    const statVerified = document.getElementById('cheq-stat-verified');

    if(statTotal) statTotal.innerHTML = `<div class="lbl">Total Issued</div><div class="val">${total}</div>`;
    if(statTransit) statTransit.innerHTML = `<div class="lbl">In Transit</div><div class="val">${inTransit}</div>`;
    if(statVerify) statVerify.innerHTML = `<div class="lbl">To Verify</div><div class="val">${toVerify}</div>`;
    if(statVerified) statVerified.innerHTML = `<div class="lbl">Verified</div><div class="val">${verified}</div>`;

    if(sorted.length === 0){
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">No cheques found.</td></tr>';
      return;
    }

    tbody.innerHTML = sorted.map(c => {
      let action = '—';
      // Land owner can mark as delivered if cheque is Issued or In_Transit
      if((c.issueDate && c.deliveryStatus === 'In_Transit') || c.deliveryStatus === 'Pending'){
        action = `<button class="btn btn-amb btn-sm" disabled title="The factory records delivery">📦 Delivered</button>`;
      } else if(c.deliveryStatus === 'Delivered' && c.verificationStatus === 'Pending'){
        action = `<button class="btn btn-p btn-sm" onclick="verifyCheq(${c.chequeId})">✓ Verify</button>`;
      }

      return `<tr>
        <td>CHQ-${String(c.chequeId).padStart(4,'0')}</td>
        <td>LN-${String(c.loanId).padStart(3,'0')}</td>
        <td>Rs ${Number(c.loanAmount || 0).toLocaleString()}</td>
        <td>${escapeHtml(c.supervisorName || '—')}</td>
        <td>${ownerChequeStatusBadge(c)}</td>
        <td>${action}</td>
      </tr>`;
    }).join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</td></tr>';
    console.warn('loadMyCheques:', e.message);
  }
}

function initBillControls(){
  const monthSel = document.getElementById('bill-mon');
  const yearInput = document.getElementById('bill-year');
  if(!monthSel || !yearInput) return;

  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  monthSel.innerHTML = monthNames
    .map((name, i) => `<option value="${i + 1}">${escapeHtml(name)}</option>`)
    .join('');

  const now = new Date();
  monthSel.value = String(now.getMonth() + 1);
  yearInput.value = String(now.getFullYear());
}

function initIncomeStatementControls(){
  const monthSel = document.getElementById('inc-end-month');
  const yearInput = document.getElementById('inc-end-year');
  if(!monthSel || !yearInput) return;

  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  monthSel.innerHTML = monthNames
    .map((name, i) => `<option value="${i + 1}">${escapeHtml(name)}</option>`)
    .join('');

  const now = new Date();
  monthSel.value = String(now.getMonth() + 1);
  yearInput.value = String(now.getFullYear());
}

function renderOwnerBill(bill){
  ownerMonthlyBillCache = bill;
  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  const label = `${monthNames[(bill.month || 1) - 1]} ${bill.year}`;

  document.getElementById('bill-mon-lbl').textContent = label;
  document.getElementById('bill-gross').textContent = formatCurrency(bill.totalEarnings);
  document.getElementById('bill-delivery').textContent = formatCurrency(bill.deliveryDeductions || 0);
  document.getElementById('bill-loan').textContent = formatCurrency(bill.loanDeductions);
  document.getElementById('bill-fert').textContent = formatCurrency(bill.fertilizerDeductions);
  document.getElementById('bill-deductions').textContent = formatCurrency(bill.totalDeductions);
  document.getElementById('bill-net').textContent = formatCurrency(bill.netPayable);
  document.getElementById('bill-generated-date').textContent = bill.generatedDate || '-';
  document.getElementById('bill-out').style.display = 'block';
}

function exportOwnerMonthlyBillPDF(){
  const bill = ownerMonthlyBillCache;
  if(!bill){
    toast('Generate a monthly bill first.', true);
    return;
  }

  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  const period = `${monthNames[(bill.month || 1) - 1]} ${bill.year}`;

  const html = `
    <html>
      <head>
        <title>Monthly Bill</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 24px; color: #1a2e24; }
          h1 { margin: 0 0 8px 0; font-size: 22px; }
          .meta { margin-bottom: 16px; color: #5a6b61; font-size: 13px; }
          .box { border: 1px solid #d9d9d9; border-radius: 8px; padding: 14px; }
          .row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
          .emph { font-weight: 700; border-top: 1px solid #ccc; margin-top: 8px; padding-top: 8px; }
        </style>
      </head>
      <body>
        <h1>Monthly Bill</h1>
        <div class="meta">Period: ${period} | Generated: ${bill.generatedDate || '-'}</div>
        <div class="box">
          <div class="row"><span>Gross Earnings</span><strong>${formatCurrency(bill.totalEarnings)}</strong></div>
          <div class="row"><span>Delivery Deductions</span><strong>${formatCurrency(bill.deliveryDeductions || 0)}</strong></div>
          <div class="row"><span>Loan / Advance Deductions</span><strong>${formatCurrency(bill.loanDeductions)}</strong></div>
          <div class="row"><span>Fertilizer Deductions</span><strong>${formatCurrency(bill.fertilizerDeductions)}</strong></div>
          <div class="row"><span>Total Deductions</span><strong>${formatCurrency(bill.totalDeductions)}</strong></div>
          <div class="row emph"><span>Net Payable</span><strong>${formatCurrency(bill.netPayable)}</strong></div>
        </div>
      </body>
    </html>
  `;

  const printWindow = window.open('', '_blank');
  if(!printWindow){
    toast('Please allow popups to export PDF.', true);
    return;
  }
  printWindow.document.open();
  printWindow.document.write(html);
  printWindow.document.close();
  printWindow.focus();
  printWindow.print();
}

function viewOwnerBill(index){
  const bill = ownerBillCache[index];
  if(bill) renderOwnerBill(bill);
}

async function genBill(){
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);
  const month = parseInt(document.getElementById('bill-mon').value, 10);
  const year = parseInt(document.getElementById('bill-year').value, 10);

  if(!Number.isFinite(landOwnerId)){ toast('Please sign in again.', true); return; }
  if(!Number.isFinite(month) || month < 1 || month > 12){ toast('Select a valid month.', true); return; }
  if(!Number.isFinite(year) || year < 2000 || year > 3000){ toast('Enter a valid year.', true); return; }

  try {
    const res = await TeaGo.request(`${API}/api/bills/generate/${landOwnerId}?month=${month}&year=${year}`, { method: 'POST' });
    const data = await res.json();

    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to generate monthly bill'), true);
      return;
    }

    renderOwnerBill(data);
    await loadBillHistory();
    toast('✓ Monthly bill generated');
  } catch(e){
    toast('❌ Cannot reach server.', true);
  }
}

async function loadBillHistory(){
  const tbody = document.getElementById('bill-history-tbody');
  if(!tbody) return;

  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--muted);padding:1rem;">Please sign in again.</td></tr>';
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/bills/owner/${landOwnerId}`);
    const list = res.ok ? await res.json() : [];

    if(!Array.isArray(list) || list.length === 0){
      ownerBillCache = [];
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--muted);padding:1.2rem;">No monthly bills generated yet.</td></tr>';
      return;
    }

    const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    ownerBillCache = list
      .slice()
      .sort((a,b) => (b.year - a.year) || (b.month - a.month));

    tbody.innerHTML = ownerBillCache
      .map((b, idx) => `
        <tr>
          <td>Monthly Bill</td>
          <td>${monthNames[(b.month || 1) - 1]} ${b.year}</td>
          <td>${b.generatedDate || '-'}</td>
          <td><span class="b g">Ready</span></td>
          <td><button class="btn btn-o btn-sm" onclick="viewOwnerBill(${idx})">View</button></td>
        </tr>
      `)
      .join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot load bills.</td></tr>';
  }
}

async function genIncomeStatement(){
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  const months = parseInt(document.getElementById('inc-per').value, 10);
  const endMonth = parseInt(document.getElementById('inc-end-month').value, 10);
  const endYear = parseInt(document.getElementById('inc-end-year').value, 10);

  if(!Number.isFinite(landOwnerId)){ toast('Please sign in again.', true); return; }
  if(!Number.isFinite(months) || months < 1){ toast('Select a valid period.', true); return; }
  if(!Number.isFinite(endMonth) || endMonth < 1 || endMonth > 12){ toast('Select a valid ending month.', true); return; }
  if(!Number.isFinite(endYear) || endYear < 2000 || endYear > 3000){ toast('Enter a valid ending year.', true); return; }

  try {
    const res = await TeaGo.request(`${API}/api/bills/statement/owner/${landOwnerId}?months=${months}&endMonth=${endMonth}&endYear=${endYear}`);
    const data = await res.json();

    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to generate income statement'), true);
      return;
    }

    renderIncomeStatement(data);
    toast('✓ Income statement generated');
  } catch(e){
    toast('❌ Cannot reach server.', true);
  }
}

function renderIncomeStatement(stmt){
  ownerIncomeStatementCache = stmt;
  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  document.getElementById('inc-title').textContent = `${stmt.periodMonths}-Month Income Statement`;

  const monthsEl = document.getElementById('inc-months');
  const rows = Array.isArray(stmt.months) ? stmt.months : [];
  monthsEl.innerHTML = rows
    .map(m => `
      <div style="display:flex;justify-content:space-between;">
        <span>${monthNames[(m.month || 1) - 1]} ${m.year}</span>
        <strong>${formatCurrency(m.netPayable)}</strong>
      </div>
    `)
    .join('');

  document.getElementById('inc-total-earnings').textContent = formatCurrency(stmt.totalEarnings);
  document.getElementById('inc-total-deductions').textContent = formatCurrency(stmt.totalDeductions);
  document.getElementById('inc-total-net').textContent = formatCurrency(stmt.totalNetPayable);
  document.getElementById('inc-generated-date').textContent = stmt.generatedDate || '-';
  document.getElementById('inc-out').style.display = 'block';
}

function exportOwnerIncomeStatementPDF(){
  const stmt = ownerIncomeStatementCache;
  if(!stmt){
    toast('Generate an income statement first.', true);
    return;
  }

  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  const rows = Array.isArray(stmt.months) ? stmt.months : [];
  const rowsHtml = rows
    .map(m => `<tr><td>${monthNames[(m.month || 1) - 1]} ${m.year}</td><td style="text-align:right;">${formatCurrency(m.totalEarnings)}</td><td style="text-align:right;">${formatCurrency(m.totalDeductions)}</td><td style="text-align:right;">${formatCurrency(m.netPayable)}</td></tr>`)
    .join('');

  const html = `
    <html>
      <head>
        <title>Income Statement</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 24px; color: #1a2e24; }
          h1 { margin: 0 0 8px 0; font-size: 22px; }
          .meta { margin-bottom: 16px; color: #5a6b61; font-size: 13px; }
          table { width: 100%; border-collapse: collapse; margin-top: 8px; }
          th, td { border: 1px solid #d9d9d9; padding: 8px; font-size: 13px; }
          th { background: #f4f4f4; text-align: left; }
          .totals { margin-top: 16px; width: 320px; margin-left: auto; }
          .totals div { display: flex; justify-content: space-between; padding: 3px 0; }
          .final { font-weight: 700; border-top: 1px solid #ccc; margin-top: 6px; padding-top: 6px; }
        </style>
      </head>
      <body>
        <h1>${stmt.periodMonths}-Month Income Statement</h1>
        <div class="meta">Land Owner: ${escapeHtml(stmt.landOwnerName || '-')} | Generated: ${stmt.generatedDate || '-'}</div>
        <table>
          <thead>
            <tr><th>Month</th><th style="text-align:right;">Earnings</th><th style="text-align:right;">Deductions</th><th style="text-align:right;">Net Payable</th></tr>
          </thead>
          <tbody>${rowsHtml}</tbody>
        </table>
        <div class="totals">
          <div><span>Total Earnings</span><strong>${formatCurrency(stmt.totalEarnings)}</strong></div>
          <div><span>Total Deductions</span><strong>${formatCurrency(stmt.totalDeductions)}</strong></div>
          <div class="final"><span>Total Net Payable</span><strong>${formatCurrency(stmt.totalNetPayable)}</strong></div>
        </div>
      </body>
    </html>
  `;

  const printWindow = window.open('', '_blank');
  if(!printWindow){
    toast('Please allow popups to export PDF.', true);
    return;
  }
  printWindow.document.open();
  printWindow.document.write(html);
  printWindow.document.close();
  printWindow.focus();
  printWindow.print();
}

async function submitComp(){
  const factorySel = document.getElementById('c-factory');
  const factoryOwnerId = parseInt(factorySel.value, 10);
  const subj = document.getElementById('c-subj').value.trim();
  const text = document.getElementById('c-text').value.trim();
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){ toast('Please sign in again to submit complaint.', true); return; }
  if(!complaintFactoryOptionsLoaded || !Number.isFinite(factoryOwnerId)){ toast('Please select a factory owner.', true); return; }
  if(!subj || !text){ toast('Please fill all fields.', true); return; }

  const btn = document.querySelector('#p-complaints .card .btn.btn-p');
  const oldText = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Submitting...';

  try {
    const res = await TeaGo.request(`${API}/api/complaints`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        landOwnerId,
        factoryOwnerId,
        complaintText: `${subj}\n${text}`
      })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to submit complaint'), true);
      return;
    }

    document.getElementById('c-subj').value = '';
    document.getElementById('c-text').value = '';
    await loadMyComplaints();
    toast('✓ Complaint submitted');
  } catch(e){
    toast('❌ Network error — is the server running?', true);
    console.warn('submitComp:', e.message);
  } finally {
    btn.disabled = false;
    btn.textContent = oldText;
  }
}

async function loadMyComplaints(){
  const openEl = document.getElementById('comp-open-list');
  const resolvedEl = document.getElementById('comp-resolved-list');
  const nb = document.getElementById('nb-comp');
  if(!openEl || !resolvedEl) return;

  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);
  if(!Number.isFinite(landOwnerId)){
    openEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">Please sign in again.</div>';
    resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">Please sign in again.</div>';
    if(nb) nb.textContent = '0';
    return;
  }

  openEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading...</div>';
  resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading...</div>';

  try {
    const res = await TeaGo.request(`${API}/api/complaints/owner/${landOwnerId}`);
    if(!res.ok){
      openEl.innerHTML = `<div style="text-align:center;color:var(--danger);padding:1rem;">❌ Could not load complaints (${res.status})</div>`;
      resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">—</div>';
      if(nb) nb.textContent = '0';
      return;
    }

    const rows = await res.json();
    const all = Array.isArray(rows) ? rows : [];
    const openRows = all.filter(c => c.status === 'Pending');
    const resolvedRows = all.filter(c => c.status === 'Resolved');

    if(nb) nb.textContent = String(openRows.length);

    if(openRows.length === 0){
      openEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No open complaints.</div>';
    } else {
      openEl.innerHTML = openRows.map(c => {
        const lines = String(c.complaintText || '').split('\n');
        const subject = lines[0] || 'Complaint';
        const detail = lines.slice(1).join(' ').trim() || '';
        return `<div class="ai"><div class="adot y">📣</div><div class="at" style="flex:1">
          <div class="am">${subject}</div>
          <div class="ti">${c.dateSubmitted || '—'} · <span class="b y" style="font-size:0.68rem;">Open</span></div>
          ${detail ? `<div style="margin-top:5px;font-size:0.81rem;color:var(--muted);">${detail}</div>` : ''}
          <div style="margin-top:5px;font-size:0.81rem;color:var(--muted);background:var(--cream);padding:0.45rem 0.7rem;border-radius:8px;">Awaiting factory response...</div>
        </div></div>`;
      }).join('');
    }

    if(resolvedRows.length === 0){
      resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No resolved complaints yet.</div>';
    } else {
      resolvedEl.innerHTML = resolvedRows.map(c => {
        const lines = String(c.complaintText || '').split('\n');
        const subject = lines[0] || 'Complaint';
        const detail = lines.slice(1).join(' ').trim() || '';
        return `<div class="ai"><div class="adot g">✅</div><div class="at" style="flex:1">
          <div class="am">${subject}</div>
          <div class="ti">${c.dateSubmitted || '—'} · <span class="b g" style="font-size:0.68rem;">Resolved</span></div>
          ${detail ? `<div style="margin-top:5px;font-size:0.81rem;color:var(--muted);">${detail}</div>` : ''}
          <div style="margin-top:5px;font-size:0.81rem;color:var(--muted);background:var(--cream);padding:0.45rem 0.7rem;border-radius:8px;"><strong>Factory:</strong> ${escapeHtml(c.responseText || 'Resolved')}</div>
        </div></div>`;
      }).join('');
    }
  } catch(e){
    openEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</div>';
    resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</div>';
    if(nb) nb.textContent = '0';
    console.warn('loadMyComplaints:', e.message);
  }
}

function toNumber(value){
  const n = Number(value);
  return Number.isFinite(n) ? n : 0;
}

function parseSafeDate(value){
  if(!value) return null;
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? null : d;
}

function isSameMonth(dateObj, ref){
  return dateObj && dateObj.getFullYear() === ref.getFullYear() && dateObj.getMonth() === ref.getMonth();
}

function monthKey(dateObj){
  return `${dateObj.getFullYear()}-${String(dateObj.getMonth() + 1).padStart(2, '0')}`;
}

function compactCurrency(value){
  const amount = toNumber(value);
  if(amount >= 1000){
    return `Rs ${Math.round(amount / 1000)}k`;
  }
  return `Rs ${Math.round(amount).toLocaleString('en-IN')}`;
}

function fetchArrayOrEmpty(url){
  return TeaGo.request(url)
    .then(res => (res.ok ? res.json() : []))
    .then(data => (Array.isArray(data) ? data : []))
    .catch(() => []);
}

function agoLabel(dateObj){
  if(!dateObj) return '-';
  const nowMs = Date.now();
  const diffMs = Math.max(0, nowMs - dateObj.getTime());
  const mins = Math.floor(diffMs / 60000);
  if(mins < 60) return mins <= 1 ? 'just now' : `${mins} mins ago`;
  const hours = Math.floor(mins / 60);
  if(hours < 24) return hours === 1 ? '1 hour ago' : `${hours} hours ago`;
  const days = Math.floor(hours / 24);
  if(days < 7) return days === 1 ? '1 day ago' : `${days} days ago`;
  return dateObj.toLocaleDateString('en-GB', { year: 'numeric', month: 'short', day: '2-digit' });
}

function renderRecentActivity(items){
  const host = document.getElementById('dash-recent-activity');
  if(!host) return;

  if(!items.length){
    host.innerHTML = '<div class="ai"><div class="adot b">i</div><div class="at"><div class="am">No recent records yet</div><div class="ti">Activity will appear here</div></div></div>';
    return;
  }

  host.innerHTML = items
    .slice(0, 5)
    .map(a => `
      <div class="ai">
        <div class="adot ${a.dotClass}">${a.icon}</div>
        <div class="at">
          <div class="am">${a.message}</div>
          <div class="ti">${agoLabel(a.when)}</div>
        </div>
      </div>
    `)
    .join('');
}

function buildCharts(deliveries){
  const chartEl = document.getElementById('del-chart');
  if(!chartEl) return;

  const now = new Date();
  const months = [];
  for(let i = 5; i >= 0; i -= 1){
    months.push(new Date(now.getFullYear(), now.getMonth() - i, 1));
  }

  const bucket = new Map(months.map(m => [monthKey(m), 0]));
  (Array.isArray(deliveries) ? deliveries : []).forEach(d => {
    const dDate = parseSafeDate(d.deliveryDate);
    if(!dDate) return;
    const key = monthKey(dDate);
    if(!bucket.has(key)) return;
    bucket.set(key, bucket.get(key) + toNumber(d.teaWeight));
  });

  const series = months.map(m => ({
    label: m.toLocaleDateString('en-GB', { month: 'short' }),
    value: bucket.get(monthKey(m)) || 0
  }));

  const max = Math.max(1, ...series.map(s => s.value));
  chartEl.innerHTML = series
    .map((s, i) => `<div class="bc"><div class="bar ${i===series.length-1?'hi':''}" style="height:${Math.max(6, Math.round((s.value / max) * 100))}px"></div><div class="blbl">${s.label}</div></div>`)
    .join('');
}

async function loadDashboardOverview(){
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);
  if(!Number.isFinite(landOwnerId)) return;

  try {
    const [deliveries, payments, loans, fertilizers, employees, cheques, liveRate] = await Promise.all([
      fetchArrayOrEmpty(`${API}/api/deliveries/owner/${landOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/payments/owner/${landOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/loans/owner/${landOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/fertilizer/owner/${landOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/employees/owner/${landOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/cheques/owner/${landOwnerId}`),
      fetchActiveFactoryRate(landOwnerId)
    ]);

    const now = new Date();
    const thisMonthDeliveries = deliveries.filter(d => isSameMonth(parseSafeDate(d.deliveryDate), now));
    const deliveredKg = thisMonthDeliveries.reduce((sum, d) => sum + toNumber(d.teaWeight), 0);

    const paymentsThisMonth = payments.filter(p => isSameMonth(parseSafeDate(p.paymentDate), now));
    const grossFromPayments = paymentsThisMonth.reduce((sum, p) => sum + toNumber(p.totalAmount), 0);
    const fallbackRate = liveRate !== null ? toNumber(liveRate) : toNumber(deliveries[0]?.ratePerKg || 50);
    const grossFromDeliveryRate = deliveredKg * fallbackRate;
    const grossIncome = grossFromPayments > 0 ? grossFromPayments : grossFromDeliveryRate;

    const loansThisMonth = loans
      .filter(l => l.approvalStatus === 'Approved' && isSameMonth(parseSafeDate(l.requestDate), now));
    const fertilizerThisMonth = fertilizers
      .filter(f => f.approvalStatus === 'Approved' && isSameMonth(parseSafeDate(f.requestDate || f.requestedDate), now));

    const monthlyLoanDeduction = loansThisMonth.reduce((sum, l) => sum + toNumber(l.loanAmount), 0);
    const monthlyFertilizerDeduction = fertilizerThisMonth.reduce((sum, f) => sum + toNumber(f.cost), 0);
    const totalDeductions = monthlyLoanDeduction + monthlyFertilizerDeduction;
    const netPayable = Math.max(0, grossIncome - totalDeductions);

    const outstandingLoan = loans
      .filter(l => l.approvalStatus === 'Approved')
      .reduce((sum, l) => sum + toNumber(l.loanAmount), 0);

    const fertilizerQtyMonth = fertilizerThisMonth.reduce((sum, f) => sum + toNumber(f.quantity), 0);
    const monthName = now.toLocaleDateString('en-GB', { month: 'long' });

    const deliveredEl = document.getElementById('dash-delivered-kg');
    const deliveredSubEl = document.getElementById('dash-delivered-sub');
    const netEl = document.getElementById('dash-net-payable');
    const loanBalEl = document.getElementById('dash-loan-balance');
    const workersEl = document.getElementById('dash-active-workers');
    const fertEl = document.getElementById('dash-fertilizer-used');
    const fertSubEl = document.getElementById('dash-fertilizer-sub');

    if(deliveredEl) deliveredEl.textContent = Math.round(deliveredKg).toLocaleString('en-IN');
    if(deliveredSubEl) deliveredSubEl.textContent = 'kg of tea leaves';
    if(netEl) netEl.textContent = compactCurrency(netPayable);
    if(loanBalEl) loanBalEl.textContent = compactCurrency(outstandingLoan);
    if(workersEl) workersEl.textContent = String(employees.length);
    if(fertEl) fertEl.textContent = `${Number(fertilizerQtyMonth.toFixed(2)).toLocaleString('en-IN')} kg`;
    if(fertSubEl) fertSubEl.textContent = monthName;

    const grossLbl = document.getElementById('dash-breakdown-gross-label');
    const grossVal = document.getElementById('dash-breakdown-gross');
    const grossBar = document.getElementById('dash-breakdown-gross-bar');
    const loanVal = document.getElementById('dash-breakdown-loan');
    const loanBar = document.getElementById('dash-breakdown-loan-bar');
    const fertVal = document.getElementById('dash-breakdown-fertilizer');
    const fertBar = document.getElementById('dash-breakdown-fertilizer-bar');
    const netVal = document.getElementById('dash-breakdown-net');

    if(grossLbl) grossLbl.textContent = `Gross Income (${Math.round(deliveredKg).toLocaleString('en-IN')}kg x Rs ${Math.round(fallbackRate).toLocaleString('en-IN')})`;
    if(grossVal) grossVal.textContent = formatCurrency(grossIncome);
    if(grossBar) grossBar.style.width = '100%';
    if(loanVal) loanVal.textContent = `-${formatCurrency(monthlyLoanDeduction)}`;
    if(loanBar) loanBar.style.width = `${grossIncome > 0 ? Math.min(100, (monthlyLoanDeduction / grossIncome) * 100) : 0}%`;
    if(fertVal) fertVal.textContent = `-${formatCurrency(monthlyFertilizerDeduction)}`;
    if(fertBar) fertBar.style.width = `${grossIncome > 0 ? Math.min(100, (monthlyFertilizerDeduction / grossIncome) * 100) : 0}%`;
    if(netVal) netVal.textContent = formatCurrency(netPayable);

    const activity = [];
    deliveries.forEach(d => {
      const when = parseSafeDate(d.deliveryDate);
      if(!when) return;
      const amount = toNumber(d.totalAmount) || (toNumber(d.teaWeight) * toNumber(d.ratePerKg || fallbackRate));
      activity.push({
        when,
        icon: '🍃',
        dotClass: 'g',
        message: `Delivered ${toNumber(d.teaWeight).toLocaleString('en-IN')} kg - ${formatCurrency(amount)}`
      });
    });
    payments.forEach(p => {
      const when = parseSafeDate(p.paymentDate);
      if(!when) return;
      activity.push({
        when,
        icon: '💰',
        dotClass: 'y',
        message: `Payment ${formatCurrency(toNumber(p.totalAmount))} recorded`
      });
    });
    loans.forEach(l => {
      const when = parseSafeDate(l.requestDate);
      if(!when) return;
      activity.push({
        when,
        icon: '📋',
        dotClass: 'b',
        message: `Loan ${formatCurrency(toNumber(l.loanAmount))} submitted`
      });
    });
    fertilizers.forEach(f => {
      const when = parseSafeDate(f.requestDate || f.requestedDate);
      if(!when) return;
      const action = f.approvalStatus === 'Approved' ? 'approved' : 'requested';
      activity.push({
        when,
        icon: '🌿',
        dotClass: 'g',
        message: `Fertilizer ${toNumber(f.quantity).toLocaleString('en-IN')} kg ${action}`
      });
    });
    cheques.forEach(c => {
      const when = parseSafeDate(c.deliveredDate || c.issuedDate || c.createdDate || c.updatedDate || c.date);
      if(!when) return;
      activity.push({
        when,
        icon: '💳',
        dotClass: 'y',
        message: `Cheque CHQ-${String(c.chequeId || '').padStart(4, '0')} ${String(c.deliveryStatus || 'issued').toLowerCase()}`
      });
    });

    activity.sort((a, b) => b.when - a.when);
    renderRecentActivity(activity);
    buildCharts(deliveries);
  } catch(e){
    console.warn('loadDashboardOverview:', e.message);
  }
}


async function loadLiveRate(){
  const landOwnerId = localStorage.getItem('teago_landOwnerId');
  if(!landOwnerId) return;

  const rateEl = document.getElementById('del-stat-rate');
  if(rateEl) rateEl.textContent = '⏳';

  const rate = await fetchActiveFactoryRate(landOwnerId);

  if(rateEl){
    rateEl.textContent = rate !== null ? 'Rs ' + rate : 'Rs 50';
  }
}

async function loadMyDeliveries(){
  const landOwnerId = localStorage.getItem('teago_landOwnerId');
  if(!landOwnerId) return;

  const tbody = document.getElementById('del-tbody');
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">⏳ Loading…</td></tr>`;

  try {
    
    const [delRes, rateRes] = await Promise.all([
      TeaGo.request(`${API}/api/deliveries/owner/${landOwnerId}`),
      fetchActiveFactoryRate(landOwnerId)
    ]);

    if(!delRes.ok){
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--danger);padding:1.5rem;">❌ Could not load deliveries (${delRes.status})</td></tr>`;
      return;
    }

    const deliveries = await delRes.json();
    tbody.innerHTML = '';

    if(!Array.isArray(deliveries) || deliveries.length === 0){
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">No deliveries recorded yet.</td></tr>`;
      updateDeliveryStatCards([], rateRes !== null ? rateRes : 50);
      return;
    }

    
    const sorted = deliveries.slice().sort((a, b) => new Date(b.deliveryDate) - new Date(a.deliveryDate));

    sorted.forEach(d => {
      const rate  = parseFloat(d.ratePerKg  ?? rateRes);
      const deduction = parseFloat(d.deductionAmount ?? 0);
      const grossAmt = parseFloat(d.teaWeight) * rate;
      const netAmt = grossAmt - deduction;
      const fmtGross = grossAmt.toLocaleString();
      const fmtDeduction = deduction.toLocaleString();
      const fmtNet = netAmt.toLocaleString();
      
      const deductionStyle = deduction > 0 ? 'color:var(--danger);font-weight:600;' : '';
      
      tbody.insertAdjacentHTML('beforeend',
        `<tr>
           <td>${d.deliveryDate}</td>
           <td>${d.teaWeight}</td>
           <td>Rs ${rate}</td>
           <td>Rs ${fmtGross}</td>
           <td style="${deductionStyle}">Rs ${fmtDeduction}</td>
           <td><strong style="color:var(--tea)">Rs ${fmtNet}</strong></td>
           <td><span class="b g">✓ Recorded</span></td>
         </tr>`);
    });

    
    const cardRate = rateRes !== null
      ? rateRes
      : parseFloat(sorted[0]?.ratePerKg ?? 50);
    updateDeliveryStatCards(deliveries, cardRate);

  } catch(e){
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Cannot reach server.</td></tr>`;
    console.warn('loadMyDeliveries:', e.message);
  }
}


async function fetchActiveFactoryRate(landOwnerId){
  try {
    
    const regRes = await TeaGo.request(`${API}/api/factories/registrations/${landOwnerId}`);
    if(!regRes.ok) return null;

    const regs = await regRes.json();
    if(!Array.isArray(regs)) return null;

    const active = regs.find(r => r.status === 'Active');
    if(!active || !active.factoryId) return null;

 
    const rateRes = await TeaGo.request(`${API}/api/deliveries/rate/${active.factoryId}`);
    if(!rateRes.ok) return null;

    const rateData = await rateRes.json();
    
    return rateData.ratePerKg != null ? parseFloat(rateData.ratePerKg) : null;

  } catch(e){
    console.warn('fetchActiveFactoryRate:', e.message);
    return null;
  }
}



function updateDeliveryStatCards(deliveries, latestRate){
  const now      = new Date();
  const todayStr = now.toISOString().split('T')[0];   

 
  const dayOfWeek   = (now.getDay() + 6) % 7;          
  const weekStart   = new Date(now);
  weekStart.setDate(now.getDate() - dayOfWeek);
  weekStart.setHours(0, 0, 0, 0);

  let monthKg = 0, weekKg = 0, todayKg = 0;

  deliveries.forEach(d => {
    const kg    = parseFloat(d.teaWeight || 0);
    const dDate = new Date(d.deliveryDate);

   
    if(dDate.getMonth() === now.getMonth() && dDate.getFullYear() === now.getFullYear())
      monthKg += kg;

    
    if(dDate >= weekStart)
      weekKg += kg;

    
    if(String(d.deliveryDate).startsWith(todayStr))
      todayKg += kg;
  });

  const monthEl = document.getElementById('del-stat-month');
  const weekEl  = document.getElementById('del-stat-week');
  const todayEl = document.getElementById('del-stat-today');
  const rateEl  = document.getElementById('del-stat-rate');

  if(monthEl) monthEl.textContent = monthKg.toLocaleString() + ' kg';
  if(weekEl)  weekEl.textContent  = weekKg.toLocaleString()  + ' kg';
  if(todayEl) todayEl.textContent = todayKg.toLocaleString() + ' kg';
  if(rateEl)  rateEl.textContent  = 'Rs ' + latestRate;
}


let allFactoriesCache = [];   
function nav(id, el){
  baseNav(id, el);
  localStorage.setItem('teago_lastPage', id);
  if(id === 'factories') initFactoriesPage();
}

const _origOpenM = openM;
openM = function(id){
  _origOpenM(id);
  if(id === 'm-fert') prepareFertilizerFactoryOptions();
  if(id === 'm-loan') prepareLoanFactoryOptions();
};

async function initFactoriesPage(){
  await Promise.all([ loadAllFactories(), loadMyRegistrations() ]);
}

async function loadAllFactories(){
  try {
    const res = await TeaGo.request(`${API}/api/factories`);
    if(!res.ok) return;
    allFactoriesCache = await res.json();

    rebuildFactoriesTable([]);

    const el = document.getElementById('fac-count-all');
    if(el) el.textContent = allFactoriesCache.length;

    const sel = document.getElementById('reg-factory-sel');
    sel.innerHTML = '<option value="">— Select a factory —</option>';
    allFactoriesCache.forEach(f => {
      const opt = document.createElement('option');
      opt.value       = f.factoryId;
      opt.textContent = `${escapeHtml(f.factoryName)} — ${escapeHtml(f.factoryLocation || '')}`;
      sel.appendChild(opt);
    });

    sel.onchange = () => {
      const chosen = allFactoriesCache.find(f => f.factoryId == sel.value);
      const preview = document.getElementById('factory-preview');
      if(chosen){
        document.getElementById('preview-name').textContent     = chosen.factoryName || '—';
        document.getElementById('preview-location').textContent = chosen.factoryLocation || '—';
        preview.style.display = 'block';
      } else {
        preview.style.display = 'none';
      }
    };

  } catch(e){ console.warn('loadAllFactories:', e.message); }
}


async function loadMyRegistrations(){
  const landOwnerId = localStorage.getItem('teago_landOwnerId');
  if(!landOwnerId) {
    const listEl = document.getElementById('my-factories-list');
    listEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1.5rem;font-size:0.87rem;">Land owner ID not found in session. Showing available factories only.</div>';
    rebuildFactoriesTable([]);
    return;
  }

  const listEl = document.getElementById('my-factories-list');
  const tb     = document.getElementById('all-factories-tb');
  listEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1.5rem;">⏳ Loading…</div>';

  try {
    const res = await TeaGo.request(`${API}/api/factories/registrations/${landOwnerId}`);
    if(!res.ok){
      listEl.innerHTML = '<div style="color:var(--danger);padding:1rem;">❌ Could not load registrations.</div>';
      rebuildFactoriesTable([]);
      return;
    }

    const regs = await res.json();

    
    const active  = regs.filter(r => r.status === 'Active').length;
    const pending = regs.filter(r => r.status === 'Pending').length;
    const activeEl  = document.getElementById('fac-count-active');
    const pendingEl = document.getElementById('fac-count-pending');
    if(activeEl)  activeEl.textContent  = active;
    if(pendingEl) pendingEl.textContent = pending;

    
    if(regs.length === 0){
      listEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1.5rem;font-size:0.87rem;">You have not registered with any factory yet.</div>';
    } else {
      listEl.innerHTML = regs.map(r => {
        const badgeClass = r.status==='Active'?'g': r.status==='Pending'?'y':'r';
        const sizeStr    = r.landSize ? `${r.landSize} acres` : '—';
        return `<div style="border:1px solid var(--cream-dark);border-radius:12px;padding:1rem;margin-bottom:0.8rem;">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;">
            <div>
              <div style="font-weight:600;color:var(--tea-dark);font-size:0.95rem;">${escapeHtml(r.factoryName)}</div>
              <div style="font-size:0.8rem;color:var(--muted);margin-top:2px;">📍 ${escapeHtml(r.factoryLocation || '—')}</div>
              <div style="font-size:0.8rem;color:var(--muted);margin-top:2px;">🌿 Land size: ${sizeStr} &nbsp;·&nbsp; Registered: ${r.registrationDate || '—'}</div>
            </div>
            <span class="b ${badgeClass}">${r.status}</span>
          </div>
        </div>`;
      }).join('');
    }

    
    rebuildFactoriesTable(regs);

  } catch(e){
    listEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Cannot reach server.</div>';
    rebuildFactoriesTable([]);
    console.warn('loadMyRegistrations:', e.message);
  }
}

function rebuildFactoriesTable(myRegs){
  const tb = document.getElementById('all-factories-tb');
  if(!allFactoriesCache.length){ tb.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1.5rem;">No factories available.</td></tr>'; return; }

  tb.innerHTML = allFactoriesCache.map((f, i) => {
    const myReg = myRegs.find(r => r.factoryId === f.factoryId);
    let statusCell, actionCell;

    if(myReg){
      const badgeClass = myReg.status==='Active'?'g': myReg.status==='Pending'?'y':'r';
      statusCell = `<span class="b ${badgeClass}">${myReg.status}</span>`;
      actionCell = myReg.status === 'Rejected'
        ? `<button class="btn btn-o btn-sm" onclick="prefillRegister(${f.factoryId})">Re-apply</button>`
        : '—';
    } else {
      statusCell = `<span class="b" style="background:var(--cream);color:var(--muted);">Not Registered</span>`;
      actionCell = `<button class="btn btn-p btn-sm" onclick="prefillRegister(${f.factoryId})">Register</button>`;
    }

    return `<tr>
      <td>${i+1}</td>
      <td><strong>${escapeHtml(f.factoryName)}</strong></td>
      <td>${escapeHtml(f.factoryLocation || '—')}</td>
      <td>${escapeHtml(f.factoryOwnerName || '—')}</td>
      <td>${f.factoryOwnerContact || '—'}</td>
      <td>${statusCell}</td>
      <td>${actionCell}</td>
    </tr>`;
  }).join('');
}


function prefillRegister(factoryId){
  const sel = document.getElementById('reg-factory-sel');
  sel.value = factoryId;
  sel.dispatchEvent(new Event('change'));
  document.getElementById('reg-factory-size').focus();
  window.scrollTo({ top: document.getElementById('reg-factory-sel').getBoundingClientRect().top + window.scrollY - 120, behavior: 'smooth' });
}


async function registerWithFactory(){
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId      = localStorage.getItem('teago_userId');
  const landOwnerId    = parseInt(rawLandOwnerId || rawUserId, 10);
  const factoryId   = parseInt(document.getElementById('reg-factory-sel').value);
  const landSize    = document.getElementById('reg-factory-size').value;

  if(!Number.isFinite(landOwnerId)) { toast('Please sign in again to submit a registration.', true); return; }
  if(!factoryId) { toast('Please select a factory.', true); return; }
  if(!landSize)  { toast('Please enter your land size.', true); return; }

  const btn = document.querySelector('[onclick="registerWithFactory()"]');
  btn.disabled = true; btn.textContent = '⏳ Submitting…';

  try {
    const res = await TeaGo.request(`${API}/api/factories/register`, {
      method : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body   : JSON.stringify({
        landOwnerId : landOwnerId,
        factoryId   : factoryId,
        landSize    : parseFloat(landSize)
      })
    });

    const data = await res.json();
    if(!res.ok){ toast('❌ ' + (data.message || 'Registration failed.'), true); return; }

    toast(`✓ Registration submitted to ${escapeHtml(data.factoryName)} — awaiting approval`);
    document.getElementById('reg-factory-size').value = '';
    document.getElementById('factory-preview').style.display = 'none';
    document.getElementById('reg-factory-sel').value = '';

    await loadMyRegistrations();

  } catch(e){
    toast('❌ Network error — is the server running?', true);
    console.error(e);
  } finally {
    btn.disabled = false; btn.textContent = '🏭 Submit Registration';
  }
}


async function loadMyPayments(){
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId      = localStorage.getItem('teago_userId');
  const landOwnerId    = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    document.getElementById('pay-history-tbody').innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Please sign in again.</td></tr>';
    return;
  }

  try {
    const [payRes, fertRes, loanRes] = await Promise.all([
      TeaGo.request(`${API}/api/payments/owner/${landOwnerId}`),
      TeaGo.request(`${API}/api/fertilizer/owner/${landOwnerId}`),
      TeaGo.request(`${API}/api/loans/owner/${landOwnerId}`)
    ]);

    if(!payRes.ok) throw new Error("Could not load payments");
    const payments = await payRes.json();

    if(!Array.isArray(payments) || payments.length === 0){
      document.getElementById('pay-history-tbody').innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--muted);padding:1.5rem;">No payments recorded yet.</td></tr>';
      initializePaymentStats(0, 0);
      document.getElementById('stat-prev-month').textContent = 'Rs 0';
    }

    payments.sort((a, b) => new Date(b.paymentDate) - new Date(a.paymentDate));

    
    const tbody = document.getElementById('pay-history-tbody');
    tbody.innerHTML = payments.length ? payments.map(p => {
      const paymentDate = new Date(p.paymentDate).toLocaleDateString('en-GB');
      const amount = `Rs ${p.totalAmount.toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`;
      const rate = `Rs ${p.ratePerKg.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
      const weight = p.teaWeight.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

      return `<tr>
        <td>${paymentDate}</td>
        <td>${escapeHtml(p.landOwnerName || '—')}</td>
        <td>${weight}</td>
        <td>${rate}</td>
        <td><strong>${amount}</strong></td>
      </tr>`;
    }).join('') : '<tr><td colspan="5">No payments recorded yet.</td></tr>';

    const currentMonth = new Date();
    const previousMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth()-1, 1);
    const [current, previous] = await Promise.all([currentMonth,previousMonth].map(date =>
      TeaGo.json(`/api/bills/preview/owner/${landOwnerId}?month=${date.getMonth()+1}&year=${date.getFullYear()}`)));
    updatePaymentStats(current.totalEarnings,current.totalDeductions,current.netPayable,currentMonth);
    document.getElementById('stat-prev-month').textContent = formatCurrency(previous.netPayable);

  } catch(e){
    document.getElementById('pay-history-tbody').innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Cannot reach server.</td></tr>';
    initializePaymentStats(0, 0);
    document.getElementById('stat-prev-month').textContent = 'Rs 0';
    console.warn('loadMyPayments:', e.message);
  }
}

function initializePaymentStats(gross, deductions){
  updatePaymentStats(gross, deductions, Math.max(0, gross - deductions), new Date());
}

function updatePaymentStats(gross, deductions, net, monthDate){
  const monthLabel = monthDate.toLocaleDateString('en-GB', { month: 'short', year: 'numeric' });
  
  const grossFormatted = gross >= 1000 ? `Rs ${(gross / 1000).toFixed(0)}k` : `Rs ${gross}`;
  const deductFormatted = deductions >= 1000 ? `Rs ${(deductions / 1000).toFixed(0)}k` : `Rs ${deductions}`;
  const netFormatted = net >= 1000 ? `Rs ${(net / 1000).toFixed(0)}k` : `Rs ${net}`;

  document.getElementById('stat-gross').innerHTML = `<div class="lbl">Gross <span id="stat-month-lbl">${monthLabel}</span></div><div class="val">${grossFormatted}</div>`;
  document.getElementById('stat-deductions').innerHTML = `<div class="lbl">Deductions</div><div class="val">${deductFormatted}</div>`;
  document.getElementById('stat-net').innerHTML = `<div class="lbl">Net Payable</div><div class="val">${netFormatted}</div>`;
}

async function signOut(){
  try { await TeaGo.logout(); } catch (error) { toast(error.message, true); }
}

function toast(msg,err=false){
  const t=document.getElementById('toast');
  t.textContent=msg;t.style.display='block';
  t.style.background=err?'var(--danger)':'var(--tea-dark)';
  clearTimeout(t._t);t._t=setTimeout(()=>t.style.display='none',3000);
}


async function loadTeaCollections(){
  const tbody = document.getElementById('tea-col-tbody');
  if(!tbody) return;

  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">Please sign in again to load tea collections.</td></tr>';
    return;
  }

  tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">⏳ Loading tea collections...</td></tr>';

  try {
    const res = await TeaGo.request(`${API}/api/tea-collections/owner/${landOwnerId}`);
    if(!res.ok){
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--danger);padding:1.5rem;">❌ Could not load tea collections (${res.status})</td></tr>`;
      return;
    }

    const collections = await res.json();
    if(!Array.isArray(collections) || collections.length === 0){
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">No tea collections recorded yet.</td></tr>';
      return;
    }

    tbody.innerHTML = collections
      .slice()
      .reverse()
      .map(c => {
        const date = new Date(c.collectionDate).toLocaleDateString('en-US', {year: 'numeric', month: 'short', day: 'numeric'});
        const qty = parseFloat(c.quantityKg).toFixed(2);
        const quality = c.quality || '—';
        const notes = c.notes || '—';
        const empName = c.employeeName || 'Unknown Employee';
        return `<tr>
          <td>${date}</td>
          <td>${empName}</td>
          <td>${qty} kg</td>
          <td>${quality}</td>
          <td>${notes}</td>
          <td><button class="btn btn-o btn-sm" onclick="deleteTeaCollection(${c.collectionId})">Delete</button></td>
        </tr>`;
      }).join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Cannot reach server.</td></tr>';
    console.warn('loadTeaCollections:', e.message);
  }
}

async function loadEmployeesForDropdown(){
  const select = document.getElementById('tea-col-employee');
  if(!select) return;

  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    select.innerHTML = '<option value="">Please sign in again</option>';
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/employees/owner/${landOwnerId}`);
    if(!res.ok){
      select.innerHTML = '<option value="">Failed to load employees</option>';
      return;
    }

    const employees = await res.json();
    if(!Array.isArray(employees) || employees.length === 0){
      select.innerHTML = '<option value="">No employees found. Add employees first.</option>';
      return;
    }

    select.innerHTML = '<option value="">Select an employee...</option>' + 
      employees
        .map(e => `<option value="${e.employeeId}">${escapeHtml(e.employeeName)}</option>`)
        .join('');
  } catch(e){
    select.innerHTML = '<option value="">Error loading employees</option>';
    console.warn('loadEmployeesForDropdown:', e.message);
  }
}

async function saveTeaCollection(){
  const empId = document.getElementById('tea-col-employee').value;
  const date = document.getElementById('tea-col-date').value;
  const qty = document.getElementById('tea-col-qty').value;
  const quality = document.getElementById('tea-col-quality').value;
  const notes = document.getElementById('tea-col-notes').value;

  if(!empId || !date || !qty){
    toast('Please fill all required fields (Employee, Date, Quantity).', true);
    return;
  }

  const btn = document.querySelector('#m-tea-col .btn.btn-p');
  const oldText = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Saving...';

  try {
    const res = await TeaGo.request(`${API}/api/tea-collections`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        employeeId: parseInt(empId, 10),
        collectionDate: date,
        quantityKg: parseFloat(qty),
        quality: quality,
        notes: notes || null
      })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to record tea collection'), true);
      return;
    }

    closeM('m-tea-col');
    await loadTeaCollections();
    toast('✓ Tea collection recorded for ' + data.employeeName);
  } catch(e){
    toast('❌ Network error — is the server running?', true);
    console.warn('saveTeaCollection:', e.message);
  } finally {
    btn.disabled = false;
    btn.textContent = oldText;
  }

  document.getElementById('tea-col-employee').value = '';
  document.getElementById('tea-col-date').value = '';
  document.getElementById('tea-col-qty').value = '';
  document.getElementById('tea-col-quality').value = 'Good';
  document.getElementById('tea-col-notes').value = '';
}

async function deleteTeaCollection(collectionId){
  if(!confirm('Delete this tea collection record?')) return;

  try {
    const res = await TeaGo.request(`${API}/api/tea-collections/${collectionId}`, {
      method: 'DELETE'
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to delete tea collection'), true);
      return;
    }

    await loadTeaCollections();
    toast('✓ Tea collection deleted');
  } catch(e){
    toast('❌ Network error while deleting', true);
    console.warn('deleteTeaCollection:', e.message);
  }
}

document.addEventListener('click', (e) => {
  if(e.target.onclick && e.target.textContent.includes('Record Collection')){
    setTimeout(() => loadEmployeesForDropdown(), 100);
  }
});


function openWageEntryModal(){
  loadEmployeesForWageDropdown();
  document.getElementById('wage-entry-date').value = new Date().toISOString().split('T')[0];
  document.getElementById('wage-entry-rate').value = '';
  document.getElementById('wage-emp-select').value = '';
  openM('m-wage-sheet-nav');
  setTimeout(() => document.getElementById('wage-emp-select').focus(), 100);
}

function updateDateForWage(){
  
}

async function loadEmployeesForWageDropdown(){
  const select = document.getElementById('wage-emp-select');
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    select.innerHTML = '<option value="">Please sign in again</option>';
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/employees/owner/${landOwnerId}`);
    if(!res.ok){
      select.innerHTML = '<option value="">Failed to load employees</option>';
      return;
    }

    const employees = await res.json();
    if(!Array.isArray(employees) || employees.length === 0){
      select.innerHTML = '<option value="">No employees found</option>';
      return;
    }

    select.innerHTML = '<option value="">Select employee...</option>' + employees
      .map(e => `<option value="${e.employeeId}">${escapeHtml(e.employeeName)}</option>`)
      .join('');
  } catch(e){
    select.innerHTML = '<option value="">Error loading employees</option>';
    console.warn('loadEmployeesForWageDropdown:', e.message);
  }
}

async function saveWageEntry(btn){
  const empId = document.getElementById('wage-emp-select').value;
  const date = document.getElementById('wage-entry-date').value;
  const rateText = document.getElementById('wage-entry-rate').value.trim();

  if(!empId || !date || !rateText){
    toast('❌ Please fill all fields', true);
    return;
  }

  const rate = parseFloat(rateText);
  if(isNaN(rate) || rate < 0){
    toast('❌ Please enter a valid wage amount', true);
    return;
  }


  const oldText = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Saving...';

  try {
    const res = await TeaGo.request(`${API}/api/daily-wages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        employeeId: parseInt(empId, 10),
        wageDate: date,
        dailyRate: rate,
        notes: null
      })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to save wage'), true);
      return;
    }

    toast('✓ Wage saved: Rs ' + rate + ' for ' + data.employeeName);
    closeM('m-wage-sheet-nav');
    await loadWageSheet();
  } catch(e){
    toast('❌ Network error', true);
    console.error('saveWageEntry:', e);
  } finally {
    btn.disabled = false;
    btn.textContent = oldText;
  }
}

async function loadWageSheet(){
  const month = parseInt(document.getElementById('wage-month').value, 10);
  const year = parseInt(document.getElementById('wage-year').value, 10);
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)){
    document.getElementById('wage-sheet-body').innerHTML = '<tr><td colspan="32">Please sign in again</td></tr>';
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/daily-wages/owner/${landOwnerId}/month?month=${month}&year=${year}`);
    if(!res.ok) return;

    const wages = await res.json();
    const empRes = await TeaGo.request(`${API}/api/employees/owner/${landOwnerId}`);
    const employees = await empRes.json();
    const daysInMonth = new Date(year, month, 0).getDate();

    let headerHtml = '<tr><th style="min-width:120px;">Employee Name</th>';
    for(let i = 1; i <= daysInMonth; i++){
      headerHtml += `<th style="text-align:center;padding:0.4rem;font-size:0.75rem;">${i}</th>`;
    }
    headerHtml += '<th style="min-width:80px;">Total</th></tr>';
    document.getElementById('wage-sheet-head').innerHTML = headerHtml;

    let bodyHtml = '';
    const wageMap = {};
    wages.forEach(w => { wageMap[`${w.employeeId}-${w.wageDate}`] = w.dailyRate; });

    employees.forEach(emp => {
      let row = `<tr><td style="font-weight:600;min-width:120px;">${escapeHtml(emp.employeeName)}</td>`;
      let total = 0;
      for(let i = 1; i <= daysInMonth; i++){
        const dateStr = `${year}-${String(month).padStart(2,'0')}-${String(i).padStart(2,'0')}`;
        const rate = wageMap[`${emp.employeeId}-${dateStr}`] || 0;
        total += parseFloat(rate);
        const displayRate = parseFloat(rate) > 0 ? parseInt(parseFloat(rate)) : '-';
        row += `<td style="padding:0.4rem;text-align:center;cursor:pointer;background:${parseFloat(rate) > 0 ? '#e8f4ed' : '#f5f0e8'};border:1px solid #e8dfd0;" onclick="clickWageCell(${emp.employeeId},${i},${month},${year})">${displayRate}</td>`;
      }
      row += `<td style="font-weight:700;min-width:80px;text-align:right;padding-right:1rem;">Rs ${Math.round(total).toLocaleString()}</td></tr>`;
      bodyHtml += row;
    });
    document.getElementById('wage-sheet-body').innerHTML = bodyHtml;

    let footerHtml = '<tr><th style="min-width:120px;">Total</th>';
    let grandTotal = 0;
    for(let i = 1; i <= daysInMonth; i++){
      let dayTotal = 0;
      wages.forEach(w => { if(new Date(w.wageDate).getDate() === i) dayTotal += parseFloat(w.dailyRate); });
      grandTotal += dayTotal;
      footerHtml += `<th style="text-align:center;padding:0.4rem;font-size:0.75rem;">Rs ${Math.round(dayTotal).toLocaleString()}</th>`;
    }
    footerHtml += `<th style="text-align:right;padding-right:1rem;">Rs ${Math.round(grandTotal).toLocaleString()}</th></tr>`;
    document.getElementById('wage-sheet-foot').innerHTML = footerHtml;
  } catch(e){ console.error('loadWageSheet:', e); }
}

async function clickWageCell(empId, day, month, year){
  await loadEmployeesForWageDropdown();
  const dateStr = `${year}-${String(month).padStart(2,'0')}-${String(day).padStart(2,'0')}`;
  document.getElementById('wage-emp-select').value = empId;
  document.getElementById('wage-entry-date').value = dateStr;
  document.getElementById('wage-entry-rate').value = '';
  openM('m-wage-sheet-nav');
  setTimeout(() => document.getElementById('wage-entry-rate').focus(), 100);
}


function openTeaCollectionModal(){
  loadEmployeesForTeaDropdown();
  document.getElementById('tea-entry-date').value = new Date().toISOString().split('T')[0];
  document.getElementById('tea-entry-qty').value = '';
  document.getElementById('tea-emp-select').value = '';
  openM('m-tea-collection-nav');
  setTimeout(() => document.getElementById('tea-emp-select').focus(), 100);
}

async function loadEmployeesForTeaDropdown(){
  const select = document.getElementById('tea-emp-select');
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  try {
    const res = await TeaGo.request(`${API}/api/employees/owner/${landOwnerId}`);
    if(!res.ok) return;
    const employees = await res.json();
    select.innerHTML = '<option value="">Select employee...</option>' + employees
      .map(e => `<option value="${e.employeeId}">${escapeHtml(e.employeeName)}</option>`).join('');
  } catch(e){ console.error('loadEmployeesForTeaDropdown:', e); }
}

async function saveTeaCollectionEntry(btn){
  const empId = document.getElementById('tea-emp-select').value;
  const date = document.getElementById('tea-entry-date').value;
  const qtyText = document.getElementById('tea-entry-qty').value.trim();
  const quality = document.getElementById('tea-entry-quality').value;

  if(!empId || !date || !qtyText){
    toast('❌ Please fill all required fields', true);
    return;
  }

  const qty = parseFloat(qtyText);
  if(isNaN(qty) || qty < 0){
    toast('❌ Please enter a valid quantity', true);
    return;
  }


  const oldText = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Saving...';

  try {
    const res = await TeaGo.request(`${API}/api/tea-collections`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        employeeId: parseInt(empId, 10),
        collectionDate: date,
        quantityKg: qty,
        quality: quality,
        notes: null
      })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to save'), true);
      return;
    }

    toast('✓ Collection saved: ' + qty + ' kg from ' + data.employeeName);
    closeM('m-tea-collection-nav');
    await loadTeaCollectionSheet();
  } catch(e){
    toast('❌ Network error', true);
    console.error('saveTeaCollectionEntry:', e);
  } finally {
    btn.disabled = false;
    btn.textContent = oldText;
  }
}

async function loadTeaCollectionSheet(){
  const month = parseInt(document.getElementById('tea-month').value, 10);
  const year = parseInt(document.getElementById('tea-year').value, 10);
  const rawLandOwnerId = localStorage.getItem('teago_landOwnerId');
  const rawUserId = localStorage.getItem('teago_userId');
  const landOwnerId = parseInt(rawLandOwnerId || rawUserId, 10);

  if(!Number.isFinite(landOwnerId)) return;

  try {
    const startDate = `${year}-${String(month).padStart(2,'0')}-01`;
    const endDate = `${year}-${String(month).padStart(2,'0')}-${new Date(year, month, 0).getDate()}`;
    const res = await TeaGo.request(`${API}/api/tea-collections/owner/${landOwnerId}/date-range?startDate=${startDate}&endDate=${endDate}`);
    if(!res.ok) return;

    const collections = await res.json();
    const empRes = await TeaGo.request(`${API}/api/employees/owner/${landOwnerId}`);
    const employees = await empRes.json();
    const daysInMonth = new Date(year, month, 0).getDate();

    let headerHtml = '<tr><th style="min-width:120px;">Employee Name</th>';
    for(let i = 1; i <= daysInMonth; i++){
      headerHtml += `<th style="text-align:center;padding:0.4rem;font-size:0.75rem;">${i}</th>`;
    }
    headerHtml += '<th style="min-width:80px;">Total</th></tr>';
    document.getElementById('tea-sheet-head').innerHTML = headerHtml;

    let bodyHtml = '';
    const teaMap = {};
    collections.forEach(c => { teaMap[`${c.employeeId}-${c.collectionDate}`] = c.quantityKg; });

    employees.forEach(emp => {
      let row = `<tr><td style="font-weight:600;min-width:120px;">${escapeHtml(emp.employeeName)}</td>`;
      let total = 0;
      for(let i = 1; i <= daysInMonth; i++){
        const dateStr = `${year}-${String(month).padStart(2,'0')}-${String(i).padStart(2,'0')}`;
        const qty = teaMap[`${emp.employeeId}-${dateStr}`] || 0;
        total += parseFloat(qty);
        const displayQty = parseFloat(qty) > 0 ? parseFloat(qty).toFixed(1) : '-';
        row += `<td style="padding:0.4rem;text-align:center;cursor:pointer;background:${parseFloat(qty) > 0 ? '#e8f4ed' : '#f5f0e8'};border:1px solid #e8dfd0;" onclick="clickTeaCell(${emp.employeeId},${i},${month},${year})">${displayQty}</td>`;
      }
      row += `<td style="font-weight:700;min-width:80px;text-align:right;padding-right:1rem;">${total.toFixed(1)} kg</td></tr>`;
      bodyHtml += row;
    });
    document.getElementById('tea-sheet-body').innerHTML = bodyHtml;

    let footerHtml = '<tr><th style="min-width:120px;">Total (kg)</th>';
    let grandTotal = 0;
    for(let i = 1; i <= daysInMonth; i++){
      let dayTotal = 0;
      collections.forEach(c => { if(new Date(c.collectionDate).getDate() === i) dayTotal += parseFloat(c.quantityKg); });
      grandTotal += dayTotal;
      footerHtml += `<th style="text-align:center;padding:0.4rem;font-size:0.75rem;">${dayTotal.toFixed(1)}</th>`;
    }
    footerHtml += `<th style="text-align:right;padding-right:1rem;">${grandTotal.toFixed(1)} kg</th></tr>`;
    document.getElementById('tea-sheet-foot').innerHTML = footerHtml;
  } catch(e){ console.error('loadTeaCollectionSheet:', e); }
}

async function clickTeaCell(empId, day, month, year){
  await loadEmployeesForTeaDropdown();
  const dateStr = `${year}-${String(month).padStart(2,'0')}-${String(day).padStart(2,'0')}`;
  document.getElementById('tea-emp-select').value = empId;
  document.getElementById('tea-entry-date').value = dateStr;
  document.getElementById('tea-entry-qty').value = '';
  openM('m-tea-collection-nav');
  setTimeout(() => document.getElementById('tea-entry-qty').focus(), 100);
}
