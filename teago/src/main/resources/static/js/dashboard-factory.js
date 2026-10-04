
let currentFactoryOwnerId = null;
let currentFactoryName = '';

document.addEventListener('DOMContentLoaded', async () => {
  const sessionOk = await hydrateSession('FACTORY_OWNER');
  if(!sessionOk) return;

  const d = new Date();
  document.getElementById('date-chip').textContent =
      d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  document.getElementById('month-lbl').textContent =
      d.toLocaleDateString('en-GB', { month: 'long', year: 'numeric' });
  document.getElementById('rate-date').valueAsDate = new Date();
  document.getElementById('del-date').valueAsDate  = new Date();

  
  const fullName        = localStorage.getItem('teago_fullName')        || 'Factory Owner';
  const factoryOwnerId  = localStorage.getItem('teago_factoryOwnerId')  || '';
  const factoryLocation = localStorage.getItem('teago_factoryLocation') || '';
  const factoryName     = localStorage.getItem('teago_factoryName')     || '';
  const role            = localStorage.getItem('teago_role')            || '';

  
  if (role && role !== 'FACTORY_OWNER') {
    window.location.href = 'index.html';
    return;
  }


  const initials = fullName.trim().split(/\s+/)
      .map(w => w[0] ? w[0].toUpperCase() : '').slice(0, 2).join('');
  document.getElementById('prof-av').textContent   = initials || '?';
  document.getElementById('prof-name').textContent = fullName;
  const idPart  = factoryOwnerId ? `FO-${String(factoryOwnerId).padStart(3,'0')}` : '—';
  const locPart = factoryLocation || factoryName || 'Factory';
  document.getElementById('prof-id').textContent   = `Factory ID: ${idPart} · ${locPart}`;
  currentFactoryOwnerId = factoryOwnerId ? parseInt(factoryOwnerId, 10) : null;
  currentFactoryName = factoryName || factoryLocation || 'Factory';


  document.getElementById('greeting-name').textContent = fullName.split(' ')[0];

  loadFactoryDashboardOverview();
  window.setInterval(loadFactoryDashboardOverview, 60000);
  loadCurrentRate();         
  loadAllDeliveries();       
  loadFactoryOwners();       
  loadFactoryLoans();      
  loadFactoryCheques();     
  loadChequeIssueOptions();  
  populateOwnerDropdown();   
  loadFactoryFertilizerRequests();
  loadFactoryComplaints();
  initFactoryBillControls();
  loadFactoryBillOwners();
  loadFactoryBillHistory();
  initFactoryIncomeStatementControls();
});

const titles={dash:'Factory Overview',owners:'Land Owners',deliveries:'All Deliveries',payments:'Payment Management',loans:'Loan Requests',fertilizer:'Fertilizer Requests',cheques:'Cheque Management',reports:'Reports & Statements',complaints:'Complaints'};

function nav(id,el){
  document.querySelectorAll('.page').forEach(p=>p.classList.remove('active'));
  document.getElementById('p-'+id).classList.add('active');
  document.getElementById('tb-t').textContent=titles[id]||id;
  if(el){document.querySelectorAll('.ni').forEach(n=>n.classList.remove('on'));el.classList.add('on');}
  if(id==='loans') loadFactoryLoans();
  if(id==='cheques') loadFactoryCheques();
  if(id==='fertilizer') loadFactoryFertilizerRequests();
  if(id==='complaints') loadFactoryComplaints();
  if(id==='reports') loadFactoryBillHistory();
  if(id==='payments') loadFactoryPayments();
  if(id==='dash') loadFactoryDashboardOverview();
}

function openM(id){
  document.getElementById(id).classList.add('on');
  if(id === 'm-cheque') loadChequeIssueOptions();
}
function closeM(id){document.getElementById(id).classList.remove('on');}
document.querySelectorAll('.mo').forEach(m=>m.addEventListener('click',e=>{if(e.target===m)m.classList.remove('on');}));

function quickApprove(btn,type){
  btn.closest('[style]').remove();
  toast('✓ '+(type==='loan'?'Loan':'Fertilizer request')+' approved');
}
function quickReject(btn){btn.closest('[style]').remove();toast('Request rejected',true);}


let currentLoanApprovalContext = {
  loanId: null,
  landOwnerName: '',
  loanAmount: 0
};

function showLoanPaymentModal(loanId, landOwnerName, loanAmount){
  currentLoanApprovalContext = { loanId, landOwnerName, loanAmount };
  document.getElementById('payment-loan-owner').textContent = landOwnerName;
  document.getElementById('payment-loan-amount').textContent = formatCurrency(loanAmount);
  openM('m-payment-method');
}

function closeLoanPaymentModal(){
  currentLoanApprovalContext = { loanId: null, landOwnerName: '', loanAmount: 0 };
  closeM('m-payment-method');
}

async function processLoanApproval(paymentMethod){
  const { loanId } = currentLoanApprovalContext;
  if(!loanId){
    toast('❌ Invalid loan ID', true);
    return;
  }

 
  try {
    const res = await TeaGo.request(`${API}/api/loans/${loanId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ approvalStatus: 'Approved' })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to approve loan'), true);
      return;
    }

    toast('✓ Loan approved');
    
    if(paymentMethod === 'cheque'){
    
      let chequeId = null;
      const chequeRes = await TeaGo.request(`${API}/api/cheques`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ loanId })
      });

      const chequeData = await chequeRes.json();
      
      if(!chequeRes.ok){
      
        if(chequeData.message && chequeData.message.includes('already')){
          toast('ℹ️ Cheque already exists for this loan', false);
        
          chequeId = chequeData.chequeId;
          if(!chequeId){
      
            const existingRes = await TeaGo.request(`${API}/api/cheques/factory/${currentFactoryOwnerId}`);
            const cheques = existingRes.ok ? await existingRes.json() : [];
            const existing = cheques.find(c => c.loanId === loanId);
            chequeId = existing?.chequeId;
          }
          
          if(!chequeId){
            toast('❌ Could not find cheque details', true);
            return;
          }
        } else {
          toast('❌ ' + (chequeData.message || 'Failed to create cheque'), true);
          return;
        }
      } else {
        chequeId = chequeData.chequeId;
      }

      const issueRes = await TeaGo.request(`${API}/api/cheques/${chequeId}/issue`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' }
      });

      if(!issueRes.ok){
        const issueData = await issueRes.json();
        
        if(!(issueData.message && issueData.message.includes('already'))){
          toast('❌ ' + (issueData.message || 'Failed to issue cheque'), true);
          return;
        }
      }

    
      const statusRes = await TeaGo.request(`${API}/api/cheques/${chequeId}/delivery-status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'In_Transit' })
      });

      if(!statusRes.ok){
        const statusData = await statusRes.json();
        toast('❌ ' + (statusData.message || 'Failed to update cheque status'), true);
        return;
      }

      toast('✓ Cheque issued and set to In Transit');
    }
    
    closeLoanPaymentModal();
    await loadFactoryLoans();
    if(paymentMethod === 'cheque'){
      await loadFactoryCheques(); 
    }
    loadFactoryDashboardOverview();
  } catch(e){
    toast('❌ Network error while processing loan', true);
    console.warn('processLoanApproval:', e.message);
  }
}

async function approveLoan(loanId){
  try {
    
    const loanRes = await TeaGo.request(`${API}/api/loans/${loanId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ approvalStatus: 'Approved' })
    });

    if(!loanRes.ok){
      const errData = await loanRes.json();
      toast('❌ ' + (errData.message || 'Failed to approve loan'), true);
      return;
    }

   
    const chequeRes = await TeaGo.request(`${API}/api/cheques`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({loanId})
    });

    const chequeData = await chequeRes.json();
    if(!chequeRes.ok){
      
      toast('✓ Loan approved (Cheque creation pending)', true);
    } else {
      toast('✓ Loan approved & Cheque issued');
    }

    await loadFactoryLoans();
    await loadFactoryCheques();
    loadFactoryDashboardOverview();
  } catch(e){
    toast('❌ Network error while approving loan', true);
    console.warn('approveLoan:', e.message);
  }
}

async function rejectLoan(loanId){
  await setLoanStatus(loanId, 'Rejected');
}

async function setLoanStatus(loanId, approvalStatus){
  try {
    const res = await TeaGo.request(`${API}/api/loans/${loanId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ approvalStatus })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to update loan'), true);
      return;
    }

    toast(approvalStatus === 'Approved' ? '✓ Loan approved' : '✓ Loan rejected');
    await loadFactoryLoans();
    loadFactoryDashboardOverview();
  } catch(e){
    toast('❌ Network error while updating loan', true);
    console.warn('setLoanStatus:', e.message);
  }
}

function formatShortDate(dateStr){
  const d = new Date(dateStr);
  if(Number.isNaN(d.getTime())) return '—';
  return d.toLocaleDateString('en-GB', { month: 'short', day: '2-digit' });
}

async function loadFactoryLoans(){
  const pendingEl = document.getElementById('loan-pending');
  const histEl = document.getElementById('loan-hist');
  if(!pendingEl || !histEl) return;

  if(!currentFactoryOwnerId){
    pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No factory session found.</div>';
    histEl.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--muted);padding:1rem;">No data.</td></tr>';
    return;
  }

  pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading loan requests...</div>';
  histEl.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading approved loans...</td></tr>';

  try {
    const res = await TeaGo.request(`${API}/api/loans/factory/${currentFactoryOwnerId}`);
    const raw = res.ok ? await res.json() : [];
    const loans = Array.isArray(raw) ? raw : [];

    
    const chequeRes = await TeaGo.request(`${API}/api/cheques/factory/${currentFactoryOwnerId}`);
    const cheques = chequeRes.ok ? await chequeRes.json() : [];
    const chequesByLoanId = {};
    cheques.forEach(c => {
      chequesByLoanId[c.loanId] = c;
    });

    const sorted = loans.slice().sort((a,b) => new Date(b.requestDate) - new Date(a.requestDate));
    const pending = sorted.filter(l => l.approvalStatus === 'Pending');
    const approved = sorted.filter(l => l.approvalStatus === 'Approved');

    const monthKey = new Date().toISOString().slice(0,7);
    const approvedThisMonth = approved.filter(l => String(l.requestDate || '').slice(0,7) === monthKey);
    const totalIssued = approved.reduce((sum, l) => sum + Number(l.loanAmount || 0), 0);
    const outstanding = totalIssued;

    const navBadge = document.getElementById('loan-nav-badge');
    if(navBadge) navBadge.textContent = String(pending.length);

    const pendingStat = document.getElementById('loan-stat-pending');
    const approvedMonthStat = document.getElementById('loan-stat-approved-month');
    const issuedStat = document.getElementById('loan-stat-issued');
    const outstandingStat = document.getElementById('loan-stat-outstanding');

    if(pendingStat) pendingStat.innerHTML = `<div class="lbl">Pending</div><div class="val">${pending.length}</div><div class="sub">Need review</div>`;
    if(approvedMonthStat) approvedMonthStat.innerHTML = `<div class="lbl">Approved This Month</div><div class="val">${approvedThisMonth.length}</div>`;
    if(issuedStat) issuedStat.innerHTML = `<div class="lbl">Total Issued</div><div class="val">${formatCurrency(totalIssued)}</div>`;
    if(outstandingStat) outstandingStat.innerHTML = `<div class="lbl">Outstanding</div><div class="val">${formatCurrency(outstanding)}</div>`;

    if(pending.length === 0){
      pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No pending loan requests.</div>';
    } else {
      pendingEl.innerHTML = pending.map(l => `
        <div class="appr-card">
          <div class="top">
            <div>
              <div class="name">${escapeHtml(l.landOwnerName || 'Land Owner')} <span class="b y" style="font-size:0.68rem;margin-left:4px;">Pending</span></div>
              <div class="detail">${escapeHtml(l.reason || 'Loan request')} · Applied ${l.requestDate || '—'}</div>
            </div>
            <div class="amount">${formatCurrency(l.loanAmount)}</div>
          </div>
          <div class="actions">
            <button class="btn btn-a btn-sm" onclick="showLoanPaymentModal(${l.loanId}, '${escapeHtml(l.landOwnerName)}', ${l.loanAmount})">✓ Approve</button>
            <button class="btn btn-d btn-sm" onclick="rejectLoan(${l.loanId})">✗ Reject</button>
          </div>
        </div>
      `).join('');
    }

    if(approved.length === 0){
      histEl.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--muted);padding:1rem;">No approved loans yet.</td></tr>';
    } else {
      histEl.innerHTML = approved.map(l => {
        const cheque = chequesByLoanId[l.loanId];

        const statusBadge = cheque && cheque.issueDate 
          ? '<span class="b g">Issued</span>' 
          : '<span class="b y">Approved</span>';
        return `
          <tr>
            <td>${escapeHtml(l.landOwnerName || '—')}</td>
            <td>${formatCurrency(l.loanAmount)}</td>
            <td>${formatShortDate(l.requestDate)}</td>
            <td>${statusBadge}</td>
          </tr>
        `;
      }).join('');
    }
  } catch(e){
    pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</div>';
    histEl.innerHTML = '<tr><td colspan="4" style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</td></tr>';
    console.warn('loadFactoryLoans:', e.message);
  }
}

async function approveFert(requestId){
  await updateFertStatus(requestId, 'Approved');
}
async function rejectFert(requestId){
  await updateFertStatus(requestId, 'Rejected');
}

async function updateFertStatus(requestId, approvalStatus){
  try {
    const res = await TeaGo.request(`${API}/api/fertilizer/${requestId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ approvalStatus })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to update fertilizer request'), true);
      return;
    }

    toast(approvalStatus === 'Approved'
      ? '✓ Fertilizer request approved'
      : '✓ Fertilizer request rejected');
    await loadFactoryFertilizerRequests();
    loadFactoryDashboardOverview();
  } catch(e){
    toast('❌ Network error while updating fertilizer request', true);
    console.warn('updateFertStatus:', e.message);
  }
}

function formatCurrency(n){
  return 'Rs ' + Number(n || 0).toLocaleString();
}

function formatQty(n){
  return Number(n || 0).toLocaleString() + ' kg';
}

async function loadFactoryFertilizerRequests(){
  const pendingEl = document.getElementById('fert-pending');
  const historyTb = document.getElementById('fert-history-tb');
  if(!pendingEl || !historyTb) return;

  if(!currentFactoryOwnerId){
    pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No factory session found.</div>';
    historyTb.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">No data.</td></tr>';
    return;
  }

  pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading...</div>';
  historyTb.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading...</td></tr>';

  try {
    const [pendingRes, allRes] = await Promise.all([
      TeaGo.request(`${API}/api/fertilizer/pending/${currentFactoryOwnerId}`),
      TeaGo.request(`${API}/api/fertilizer/factory/${currentFactoryOwnerId}`)
    ]);

    const pendingData = pendingRes.ok ? await pendingRes.json() : [];
    const allData = allRes.ok ? await allRes.json() : [];

    const pending = Array.isArray(pendingData) ? pendingData : [];
    const all = Array.isArray(allData) ? allData : [];
    const approved = all.filter(r => r.approvalStatus === 'Approved');

    const approvedQty = approved.reduce((sum, r) => sum + Number(r.quantity || 0), 0);
    const approvedCost = approved.reduce((sum, r) => sum + Number(r.cost || 0), 0);

    const pendingStat = document.getElementById('fert-stat-pending');
    const approvedStat = document.getElementById('fert-stat-approved');
    const qtyStat = document.getElementById('fert-stat-qty');
    const costStat = document.getElementById('fert-stat-cost');
    if(pendingStat) pendingStat.textContent = String(pending.length);
    if(approvedStat) approvedStat.textContent = String(approved.length);
    if(qtyStat) qtyStat.textContent = formatQty(approvedQty);
    if(costStat) costStat.textContent = formatCurrency(approvedCost);

    if(pending.length === 0){
      pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No pending fertilizer requests.</div>';
    } else {
      pendingEl.innerHTML = pending.map(r => `
        <div class="appr-card">
          <div class="top">
            <div>
              <div class="name">${escapeHtml(r.landOwnerName || 'Land Owner')}<span class="b y" style="font-size:0.68rem;margin-left:6px;">Pending</span></div>
              <div class="detail">${Number(r.quantity || 0)} kg ${r.fertilizerType || ''} · Total ${formatCurrency(r.cost)}</div>
            </div>
            <div class="amount" style="color:var(--tea);">${Number(r.quantity || 0)} kg</div>
          </div>
          <div class="actions">
            <button class="btn btn-a btn-sm" onclick="approveFert(${r.requestId})">✓ Approve</button>
            <button class="btn btn-d btn-sm" onclick="rejectFert(${r.requestId})">✗ Reject</button>
          </div>
        </div>
      `).join('');
    }

    if(approved.length === 0){
      historyTb.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">No approved requests yet.</td></tr>';
    } else {
      historyTb.innerHTML = approved
        .slice()
        .sort((a,b) => (b.requestId || 0) - (a.requestId || 0))
        .map(r => `
          <tr>
            <td>Request #${r.requestId}</td>
            <td>${escapeHtml(r.landOwnerName || '—')}</td>
            <td>${r.fertilizerType || '—'}</td>
            <td>${formatQty(r.quantity)}</td>
            <td>${formatCurrency(r.cost)}</td>
            <td><span class="b g">Approved</span></td>
          </tr>
        `).join('');
    }
  } catch(e){
    pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</div>';
    historyTb.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</td></tr>';
    console.warn('loadFactoryFertilizerRequests:', e.message);
  }
}

async function approveOwner(btn, registrationId, ownerName){
  if(!registrationId){
    toast('Missing registration id for approval.', true);
    return;
  }

  btn.disabled = true;
  const oldText = btn.textContent;
  btn.textContent = 'Approving...';

  try {
    const res = await TeaGo.request(`${API}/api/factories/registrations/${registrationId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'Active' })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Approval failed'), true);
      btn.disabled = false;
      btn.textContent = oldText;
      return;
    }

    const row = btn.closest('tr');
    row.cells[6].innerHTML = '<span class="b g">Approved</span>';
    row.cells[7].innerHTML = `<button class="btn btn-o btn-sm" onclick="toast('Viewing ${ownerName || 'owner'}')">View</button>`;
    toast('✓ Land owner approved');
  } catch(e){
    toast('❌ Network error while approving', true);
    btn.disabled = false;
    btn.textContent = oldText;
    console.warn('approveOwner:', e.message);
  }
}


async function loadFactoryOwners(){
  const tbody = document.getElementById('owners-tb');
  if(!tbody) return;

  if(!currentFactoryOwnerId){
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:var(--muted);padding:1.5rem;">No factory session found. Please sign in again.</td></tr>';
    updateLandOwnerStats([], []);
    return;
  }

  tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:var(--muted);padding:1.5rem;">⏳ Loading land owners…</td></tr>';

  try {
    const res = await TeaGo.request(`${API}/api/factories/owners/${currentFactoryOwnerId}`);
    if(!res.ok){
      let msg = `Could not load owners (${res.status})`;
      try {
        const err = await res.json();
        if(err && err.message) msg = err.message;
      } catch(_e) {}
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:var(--danger);padding:1.5rem;">❌ ${msg}</td></tr>`;
      updateLandOwnerStats([], []);
      return;
    }

    const rows = await res.json();
    if(!Array.isArray(rows) || rows.length === 0){
      tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:var(--muted);padding:1.5rem;">No registered land owners yet.</td></tr>';
      updateLandOwnerStats([], []);
      return;
    }

    updateLandOwnerStats(rows, rows.filter(r => r.status === 'Active'));
    tbody.innerHTML = rows
      .slice()
      .sort((a,b) => (a.landOwnerId || 0) - (b.landOwnerId || 0))
      .map(r => {
        const id = r.landOwnerId ? `LO-${String(r.landOwnerId).padStart(3,'0')}` : '—';
        const name = r.landOwnerName || '—';
        const nic = r.landOwnerNic || '—';
        const landSize = (r.landSize ?? '—') !== '—' ? Number(r.landSize).toFixed(2) : '—';
        const status = r.status || 'Pending';
        const statusLabel = status === 'Active' ? 'Approved' : status;
        const statusClass = status === 'Active' ? 'g' : (status === 'Rejected' ? 'r' : 'y');
        const safeName = String(name).replace(/'/g, "\\'");
        const action = status === 'Pending' && r.registrationId
          ? `<button class="btn btn-a btn-sm" onclick="approveOwner(this, ${r.registrationId}, decodeURIComponent('${encodeURIComponent(name).replace(/'/g, '%27')}'))">Approve</button>`
          : `<button class="btn btn-o btn-sm" onclick="viewLandOwnerDetails(${r.landOwnerId}, decodeURIComponent('${encodeURIComponent(name).replace(/'/g, '%27')}'))">View</button>`;

        return `<tr>
          <td>${id}</td>
          <td>${escapeHtml(name)}</td>
          <td>${escapeHtml(nic)}</td>
          <td>${landSize}</td>
          <td>—</td>
          <td>—</td>
          <td><span class="b ${statusClass}">${statusLabel}</span></td>
          <td>${action}</td>
        </tr>`;
      }).join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Cannot reach server.</td></tr>';
    updateLandOwnerStats([], []);
    console.warn('loadFactoryOwners:', e.message);
  }
}


function updateLandOwnerStats(allRows, activeRows){
  const totalEl = document.getElementById('stat-total-registered');
  const activeEl = document.getElementById('stat-active-owners');
  const pendingEl = document.getElementById('stat-pending-approval');
  const landEl = document.getElementById('stat-total-land');
  
  if(!totalEl || !activeEl || !pendingEl || !landEl) return;
  
  const totalRegistered = allRows.length;
  const activeCount = activeRows.length;
  const pendingCount = allRows.filter(r => r.status === 'Pending').length;
  const totalLand = allRows.reduce((sum, r) => sum + (parseFloat(r.landSize) || 0), 0);
  
  totalEl.textContent = totalRegistered.toLocaleString();
  activeEl.textContent = activeCount.toLocaleString();
  pendingEl.textContent = pendingCount.toLocaleString();
  landEl.textContent = totalLand.toFixed(2) + ' ac';
}

async function loadFactoryPayments(){
  const tbody = document.getElementById('pay-tb');
  if(!tbody) return;

  if(!currentFactoryOwnerId){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">No factory session found.</td></tr>';
    return;
  }

  tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">⏳ Loading payment data…</td></tr>';

  try {
    const res = await TeaGo.request(`${API}/api/payments/factory/${currentFactoryOwnerId}/summary`);
    
    if(!res.ok){
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--danger);padding:1.5rem;">❌ Could not load payment data</td></tr>';
      return;
    }

    const data = await res.json();
    
  
    const totalStat = document.getElementById('pay-stat-total');
    const deductionsStat = document.getElementById('pay-stat-deductions');
    const netStat = document.getElementById('pay-stat-net');
    const doneStat = document.getElementById('pay-stat-done');
    
    if(totalStat) totalStat.textContent = formatCurrency(data.totalPayable || 0);
    if(deductionsStat) deductionsStat.textContent = formatCurrency(data.totalDeductions || 0);
    if(netStat) netStat.textContent = formatCurrency(data.netDisbursable || 0);
    if(doneStat) doneStat.textContent = `${data.paymentsDone || 0} / ${data.totalLandOwners || 0}`;
    
   
    const summariesRes = await TeaGo.request(`${API}/api/payments/factory/${currentFactoryOwnerId}`);
    const summaries = summariesRes.ok ? await summariesRes.json() : [];
    
    if(!Array.isArray(summaries) || summaries.length === 0){
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">No payment data available.</td></tr>';
      return;
    }

    tbody.innerHTML = summaries.map(s => {
      const statusClass = s.status === 'Processed' || s.status === 'Review required' ? 'g' : 'y';
      const action = (s.status === 'Processed' || s.status === 'Review required') ? '—' : `<button class="btn btn-a btn-sm" onclick="showPaymentMethodModal(this, ${s.landOwnerId}, ${s.netAmount})">Select Payment</button>`;
      return `<tr>
        <td>${escapeHtml(s.landOwnerName || '—')}</td>
        <td>${formatCurrency(s.grossAmount)}</td>
        <td>${formatCurrency(s.deductionsAmount)}</td>
        <td>${formatCurrency(s.netAmount)}</td>
        <td><span class="b ${statusClass}">${s.status || '—'}</span></td>
        <td>${action}</td>
      </tr>`;
    }).join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1.5rem;">⚠️ Cannot reach server.</td></tr>';
    console.warn('loadFactoryPayments:', e.message);
  }
}


let currentPaymentContext = {
  landOwnerName: '',
  netAmount: 0,
  button: null
};

function showPaymentMethodModal(btn, landOwnerId, netAmount){
  const landOwnerName = btn.closest('tr').cells[0].textContent;
  currentPaymentContext = { landOwnerId, landOwnerName, netAmount, button: btn };
  document.getElementById('payment-reference').value = '';
  document.getElementById('payment-select-owner').textContent = landOwnerName;
  document.getElementById('payment-select-amount').textContent = formatCurrency(netAmount);
  openM('m-payment-select');
}

function closePaymentMethodModal(){
  currentPaymentContext = { landOwnerName: '', netAmount: 0, button: null };
  closeM('m-payment-select');
}

async function processPaymentOnline(button){ await recordPayment('BANK_TRANSFER',button); }
async function processPaymentCheque(button){ await recordPayment('CHEQUE',button); }
async function recordPayment(method,button){
  const reference=document.getElementById('payment-reference').value.trim();
  if(!reference){ toast('Enter the payment reference',true); return; }
  const now=new Date(); button.disabled=true;
  try {
    await TeaGo.json(`/api/payments/factory/${currentFactoryOwnerId}/settlements`, {
      method:'POST',headers:{'Content-Type':'application/json'},
      body:JSON.stringify({landOwnerId:currentPaymentContext.landOwnerId,month:now.getMonth()+1,year:now.getFullYear(),method,reference})
    });
    closePaymentMethodModal(); await loadFactoryPayments(); toast('External payment recorded');
  } catch(error){ toast(error.message,true); } finally { button.disabled=false; }
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
      toast('❌ ' + (data.message || 'Failed to update cheque status'), true);
      return;
    }

    toast('✓ Cheque marked as delivered');
    await loadFactoryCheques();
  } catch(e){
    toast('❌ Network error while updating cheque status', true);
    console.warn('markChequeDelivered:', e.message);
  }
}

function factoryChequeStatusBadge(cheque){
  const delivery = cheque.deliveryStatus;
  const verification = cheque.verificationStatus;
  const issueDate = cheque.issueDate;

  if(verification === 'Verified') return '<span class="b g">Verified</span>';
  if(delivery === 'Delivered' && verification === 'Pending') return '<span class="b bl">Pending Verify</span>';
  if(issueDate && delivery === 'In_Transit') return '<span class="b sage">Issued</span>'; // Cheque issued (has issueDate)
  if(delivery === 'Pending' && !issueDate) return '<span class="b y">Approved</span>'; // Awaiting issue
  if(delivery === 'In_Transit') return '<span class="b g">In Transit</span>';
  if(delivery === 'Delivered') return '<span class="b bl">Delivered</span>';
  if(delivery === 'Returned') return '<span class="b r">Returned</span>';
  return `<span class="b">${delivery || 'Pending'}</span>`;
}

async function loadFactoryCheques(){
  const tbody = document.getElementById('cheq-tb');
  if(!tbody) return;

  if(!currentFactoryOwnerId){
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1rem;">No factory session found.</td></tr>';
    return;
  }

  tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading cheques...</td></tr>';

  try {
    const res = await TeaGo.request(`${API}/api/cheques/factory/${currentFactoryOwnerId}`);
    const raw = res.ok ? await res.json() : [];
    const cheques = Array.isArray(raw) ? raw : [];
    const sorted = cheques.slice().sort((a,b) => (b.chequeId || 0) - (a.chequeId || 0));

    const pending = sorted.filter(c => c.deliveryStatus === 'Pending').length;
    const toVerify = sorted.filter(c => c.deliveryStatus === 'Delivered' && c.verificationStatus === 'Pending').length;
    const verified = sorted.filter(c => c.verificationStatus === 'Verified').length;
    const totalValue = sorted.reduce((sum, c) => sum + Number(c.loanAmount || 0), 0);

    const statPending = document.getElementById('cheq-stat-pending');
    const statTransit = document.getElementById('cheq-stat-transit');
    const statVerified = document.getElementById('cheq-stat-verified');
    const statValue = document.getElementById('cheq-stat-value');

    if(statPending) statPending.innerHTML = `<div class="lbl">Pending Delivery</div><div class="val">${pending}</div>`;
    if(statTransit) statTransit.innerHTML = `<div class="lbl">To Verify</div><div class="val">${toVerify}</div>`;
    if(statVerified) statVerified.innerHTML = `<div class="lbl">Verified</div><div class="val">${verified}</div>`;
    if(statValue) statValue.innerHTML = `<div class="lbl">Total Value</div><div class="val">Rs ${totalValue.toLocaleString()}</div>`;

    if(sorted.length === 0){
      tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1rem;">No cheque records yet.</td></tr>';
      return;
    }

    tbody.innerHTML = sorted.map(c => {
      const issueDate = c.issueDate || '—';

      return `<tr>
        <td>CHQ-${String(c.chequeId).padStart(4,'0')}</td>
        <td>${escapeHtml(c.landOwnerName || '—')}</td>
        <td>Rs ${Number(c.loanAmount || 0).toLocaleString()}</td>
        <td>${escapeHtml(c.supervisorName || '—')}</td>
        <td>${issueDate}</td>
        <td>${factoryChequeStatusBadge(c)}</td>
        <td>—</td>
      </tr>`;
    }).join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</td></tr>';
    console.warn('loadFactoryCheques:', e.message);
  }
}

async function issueCheque(){
  const loanId = parseInt(document.getElementById('cq-loan').value, 10);

  if(!Number.isFinite(loanId)){
    toast('Please select approved loan.', true);
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/cheques`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ loanId })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to issue cheque'), true);
      return;
    }

    closeM('m-cheque');
    await loadChequeIssueOptions();
    toast('✓ Cheque issued successfully');
    await loadFactoryCheques();
    loadFactoryDashboardOverview();
  } catch(e){
    toast('❌ Network error while issuing cheque', true);
    console.warn('issueCheque:', e.message);
  }
}

async function issueChequeAction(chequeId){
  if(!chequeId || chequeId <= 0){
    toast('Invalid cheque ID', true);
    return;
  }

  try {
   
    const res = await TeaGo.request(`${API}/api/cheques/${chequeId}/issue`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' }
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to issue cheque'), true);
      return;
    }

    
    const statusRes = await TeaGo.request(`${API}/api/cheques/${chequeId}/delivery-status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'In_Transit' })
    });

    const statusData = await statusRes.json();
    if(!statusRes.ok){
      toast('❌ ' + (statusData.message || 'Failed to update cheque status'), true);
      return;
    }

    toast('✓ Cheque issued and set to In Transit');
    await loadFactoryCheques();
    loadFactoryDashboardOverview();
  } catch(e){
    toast('❌ Network error while issuing cheque', true);
    console.warn('issueChequeAction:', e.message);
  }
}

async function loadChequeIssueOptions(){
  const facSel = document.getElementById('cq-factory');
  const loanSel = document.getElementById('cq-loan');
  if(!facSel || !loanSel) return;

  facSel.innerHTML = `<option value="${currentFactoryOwnerId || ''}">${currentFactoryName || 'Factory'}</option>`;

  loanSel.innerHTML = '<option value="">Loading approved loans...</option>';

  if(!currentFactoryOwnerId){
    loanSel.innerHTML = '<option value="">No factory session</option>';
    return;
  }

  try {
    const [loanRes, cheqRes] = await Promise.all([
      TeaGo.request(`${API}/api/loans/factory/${currentFactoryOwnerId}`),
      TeaGo.request(`${API}/api/cheques/factory/${currentFactoryOwnerId}`)
    ]);

    const loanRaw = loanRes.ok ? await loanRes.json() : [];
    const cheqRaw = cheqRes.ok ? await cheqRes.json() : [];

    const loans = Array.isArray(loanRaw) ? loanRaw : [];
    const cheques = Array.isArray(cheqRaw) ? cheqRaw : [];

    const issuedLoanIds = new Set(cheques.map(c => Number(c.loanId)));
    const approvedUnissued = loans
      .filter(l => l.approvalStatus === 'Approved' && !issuedLoanIds.has(Number(l.loanId)))
      .sort((a,b) => new Date(b.requestDate) - new Date(a.requestDate));

    if(approvedUnissued.length === 0){
      loanSel.innerHTML = '<option value="">No approved loans available</option>';
    } else {
      loanSel.innerHTML = '<option value="">-- Select Approved Loan --</option>';
      approvedUnissued.forEach(l => {
        const opt = document.createElement('option');
        opt.value = l.loanId;
        opt.textContent = `${escapeHtml(l.landOwnerName || 'Land Owner')} - Rs ${Number(l.loanAmount || 0).toLocaleString()} (${l.requestDate || '—'})`;
        loanSel.appendChild(opt);
      });
    }

  } catch(e){
    loanSel.innerHTML = '<option value="">Cannot load loans</option>';
    console.warn('loadChequeIssueOptions:', e.message);
  }
}

function formatCurrency(value){
  const n = Number(value || 0);
  return 'Rs ' + n.toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
}

function initFactoryBillControls(){
  const monthSel = document.getElementById('rep-month');
  const yearInput = document.getElementById('rep-year');
  if(!monthSel || !yearInput) return;

  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  monthSel.innerHTML = monthNames
    .map((name, i) => `<option value="${i + 1}">${escapeHtml(name)}</option>`)
    .join('');

  const now = new Date();
  monthSel.value = String(now.getMonth() + 1);
  yearInput.value = String(now.getFullYear());
}

async function loadFactoryBillOwners(){
  const ownerSel = document.getElementById('rep-owner');
  const incOwnerSel = document.getElementById('rep-inc-owner');
  if(!ownerSel) return;

  ownerSel.innerHTML = '<option value="ALL">All Active Land Owners</option>';
  if(incOwnerSel) incOwnerSel.innerHTML = '<option value="">Select owner</option>';

  if(!currentFactoryOwnerId) return;

  try {
    const res = await TeaGo.request(`${API}/api/factories/active-owners/${currentFactoryOwnerId}`);
    if(!res.ok) return;

    const owners = await res.json();
    if(!Array.isArray(owners)) return;

    owners.forEach(o => {
      const opt = document.createElement('option');
      opt.value = String(o.landOwnerId);
      opt.textContent = o.landOwnerName || (`Land Owner #${o.landOwnerId}`);
      ownerSel.appendChild(opt);

      if(incOwnerSel){
        const incOpt = document.createElement('option');
        incOpt.value = String(o.landOwnerId);
        incOpt.textContent = opt.textContent;
        incOwnerSel.appendChild(incOpt);
      }
    });
  } catch(e){
    console.warn('loadFactoryBillOwners:', e.message);
  }
}

function initFactoryIncomeStatementControls(){
  const monthSel = document.getElementById('rep-inc-end-month');
  const yearInput = document.getElementById('rep-inc-end-year');
  if(!monthSel || !yearInput) return;

  const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
  monthSel.innerHTML = monthNames
    .map((name, i) => `<option value="${i + 1}">${escapeHtml(name)}</option>`)
    .join('');

  const now = new Date();
  monthSel.value = String(now.getMonth() + 1);
  yearInput.value = String(now.getFullYear());
}

let factoryIncomeStatementCache = null;
let factoryBillHistoryCache = [];

async function generateFactoryIncomeStatement(){
  const ownerId = parseInt(document.getElementById('rep-inc-owner').value, 10);
  const months = parseInt(document.getElementById('rep-inc-per').value, 10);
  const endMonth = parseInt(document.getElementById('rep-inc-end-month').value, 10);
  const endYear = parseInt(document.getElementById('rep-inc-end-year').value, 10);

  if(!currentFactoryOwnerId){ toast('Please sign in again.', true); return; }
  if(!Number.isFinite(ownerId)){ toast('Select a land owner.', true); return; }
  if(!Number.isFinite(months) || months < 1){ toast('Select a valid period.', true); return; }
  if(!Number.isFinite(endMonth) || endMonth < 1 || endMonth > 12){ toast('Select a valid ending month.', true); return; }
  if(!Number.isFinite(endYear) || endYear < 2000 || endYear > 3000){ toast('Enter a valid ending year.', true); return; }

  try {
    const url = `${API}/api/bills/statement/factory/${currentFactoryOwnerId}/owner/${ownerId}?months=${months}&endMonth=${endMonth}&endYear=${endYear}`;
    const res = await TeaGo.request(url);
    const data = await res.json();

    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to generate income statement'), true);
      return;
    }

    renderFactoryIncomeStatement(data);
    toast('✓ Income statement generated');
  } catch(e){
    toast('❌ Cannot reach server.', true);
  }
}

function renderFactoryIncomeStatement(stmt){
  factoryIncomeStatementCache = stmt;
  const summary = document.getElementById('rep-summary');
  if(!summary) return;

  const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  const rows = Array.isArray(stmt.months) ? stmt.months : [];

  summary.innerHTML = `
    <div style="font-weight:600;color:var(--text);margin-bottom:0.4rem;">${stmt.periodMonths}-Month Income Statement - ${escapeHtml(stmt.landOwnerName || '-')}</div>
    <div style="display:flex;flex-direction:column;gap:0.25rem;margin-bottom:0.5rem;">
      ${rows.map(m => `<div style="display:flex;justify-content:space-between;"><span>${monthNames[(m.month || 1) - 1]} ${m.year}</span><strong>${formatCurrency(m.netPayable)}</strong></div>`).join('')}
    </div>
    <div style="border-top:1px solid var(--cream-dark);padding-top:0.4rem;display:flex;flex-direction:column;gap:0.2rem;">
      <div style="display:flex;justify-content:space-between;"><span>Total Earnings</span><strong>${formatCurrency(stmt.totalEarnings)}</strong></div>
      <div style="display:flex;justify-content:space-between;"><span>Total Deductions</span><strong>${formatCurrency(stmt.totalDeductions)}</strong></div>
      <div style="display:flex;justify-content:space-between;font-weight:700;color:var(--text);"><span>Total Net Payable</span><strong>${formatCurrency(stmt.totalNetPayable)}</strong></div>
    </div>
    <div style="margin-top:0.5rem;font-size:0.76rem;color:var(--muted);">Generated on: ${stmt.generatedDate || '-'}</div>
    <button class="btn btn-o btn-sm" style="margin-top:0.6rem;" onclick="exportFactoryIncomeStatementPDF()">⬇ Export PDF</button>
  `;
  summary.style.display = 'block';
}

function exportFactoryIncomeStatementPDF(){
  const stmt = factoryIncomeStatementCache;
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
        <title>Factory Income Statement</title>
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

async function generateFactoryBills(){
  const ownerSel = document.getElementById('rep-owner');
  const month = parseInt(document.getElementById('rep-month').value, 10);
  const year = parseInt(document.getElementById('rep-year').value, 10);
  const ownerId = ownerSel ? ownerSel.value : 'ALL';

  if(!currentFactoryOwnerId){ toast('Please sign in again.', true); return; }
  if(!Number.isFinite(month) || month < 1 || month > 12){ toast('Select a valid month.', true); return; }
  if(!Number.isFinite(year) || year < 2000 || year > 3000){ toast('Enter a valid year.', true); return; }

  let endpoint;
  if(ownerId === 'ALL') {
    endpoint = `${API}/api/bills/factory/${currentFactoryOwnerId}/generate?month=${month}&year=${year}`;
  } else {
    endpoint = `${API}/api/bills/factory/${currentFactoryOwnerId}/generate/${ownerId}?month=${month}&year=${year}`;
  }

  try {
    const res = await TeaGo.request(endpoint, { method: 'POST' });
    const data = await res.json();

    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to generate monthly bill(s)'), true);
      return;
    }

    const count = Array.isArray(data) ? data.length : 1;
    document.getElementById('rep-summary').innerHTML = `
      <div style="display:flex;flex-direction:column;gap:0.3rem;">
        <div><strong>Generation completed</strong></div>
        <div>Period: ${month}/${year}</div>
        <div>Bills generated: ${count}</div>
      </div>
    `;

    await loadFactoryBillHistory();
    toast('✓ Monthly bill generation completed');
  } catch(e){
    toast('❌ Cannot reach server.', true);
  }
}

async function loadFactoryBillHistory(){
  const tbody = document.getElementById('factory-bills-tbody');
  if(!tbody) return;

  if(!currentFactoryOwnerId){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">Please sign in again.</td></tr>';
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/bills/factory/${currentFactoryOwnerId}`);
    const list = res.ok ? await res.json() : [];

    if(!Array.isArray(list) || list.length === 0){
      factoryBillHistoryCache = [];
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">No monthly bills generated yet.</td></tr>';
      return;
    }

    const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    factoryBillHistoryCache = list
      .slice()
      .sort((a,b) => (b.year - a.year) || (b.month - a.month) || String(a.landOwnerName).localeCompare(String(b.landOwnerName)));

    tbody.innerHTML = factoryBillHistoryCache.map((b, idx) => `
      <tr>
        <td>Monthly Bill</td>
        <td>${escapeHtml(b.landOwnerName || '-')}</td>
        <td>${monthNames[(b.month || 1) - 1]} ${b.year}</td>
        <td>${b.generatedDate || '-'}</td>
        <td><strong>${formatCurrency(b.netPayable)}</strong></td>
        <td><button class="btn btn-o btn-sm" onclick="exportFactoryMonthlyBillPDF(${idx})">⬇ PDF</button></td>
      </tr>
    `).join('');
  } catch(e){
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot load bill history.</td></tr>';
  }
}

function exportFactoryMonthlyBillPDF(index){
  const bill = factoryBillHistoryCache[index];
  if(!bill){
    toast('Monthly bill not found.', true);
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
        <div class="meta">Land Owner: ${escapeHtml(bill.landOwnerName || '-')} | Period: ${period} | Generated: ${bill.generatedDate || '-'}</div>
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

async function sendComplaintResponse(complaintId, inputId){
  const input = document.getElementById(inputId);
  const responseText = input ? input.value.trim() : '';
  if(!responseText){
    toast('Please type a response.', true);
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/complaints/${complaintId}/response`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ responseText, status: 'Resolved' })
    });

    const data = await res.json();
    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to send response'), true);
      return;
    }

    toast('✓ Response sent to land owner');
    await loadFactoryComplaints();
    loadFactoryDashboardOverview();
  } catch(e){
    toast('❌ Network error while sending response', true);
    console.warn('sendComplaintResponse:', e.message);
  }
}

async function loadFactoryComplaints(){
  const pendingEl = document.getElementById('comp-pending-list');
  const resolvedEl = document.getElementById('comp-resolved-list');
  if(!pendingEl || !resolvedEl) return;

  if(!currentFactoryOwnerId){
    pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No factory session found.</div>';
    resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No factory session found.</div>';
    return;
  }

  pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading...</div>';
  resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⏳ Loading...</div>';

  try {
    const [pendingRes, allRes] = await Promise.all([
      TeaGo.request(`${API}/api/complaints/pending/${currentFactoryOwnerId}`),
      TeaGo.request(`${API}/api/complaints/factory/${currentFactoryOwnerId}`)
    ]);

    const pendingData = pendingRes.ok ? await pendingRes.json() : [];
    const allData = allRes.ok ? await allRes.json() : [];
    const pending = Array.isArray(pendingData) ? pendingData : [];
    const all = Array.isArray(allData) ? allData : [];
    const resolved = all.filter(c => c.status === 'Resolved');

    const openStat = document.getElementById('comp-stat-open');
    const resolvedStat = document.getElementById('comp-stat-resolved');
    const totalStat = document.getElementById('comp-stat-total');
    if(openStat) openStat.textContent = String(pending.length);
    if(resolvedStat) resolvedStat.textContent = String(resolved.length);
    if(totalStat) totalStat.textContent = String(all.length);

    if(pending.length === 0){
      pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No open complaints.</div>';
    } else {
      pendingEl.innerHTML = pending
        .slice()
        .sort((a,b) => (b.complaintId || 0) - (a.complaintId || 0))
        .map(c => {
          const lines = String(c.complaintText || '').split('\n');
          const subject = lines[0] || 'Complaint';
          const detail = lines.slice(1).join(' ').trim() || '';
          const inputId = `resp-${c.complaintId}`;
          return `<div style="background:rgba(200,168,75,0.07);border:1px solid rgba(200,168,75,0.18);border-radius:14px;padding:1.2rem;margin-bottom:0.9rem;">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:0.7rem;">
              <div><div style="color:var(--text);font-weight:600;">${escapeHtml(c.landOwnerName || 'Land Owner')} <span class="b y" style="font-size:0.68rem;margin-left:6px;">Open</span></div><div style="font-size:0.8rem;color:var(--muted);margin-top:2px;">${subject} · ${c.dateSubmitted || '—'}</div></div>
            </div>
            ${detail ? `<div style="font-size:0.85rem;color:var(--muted);margin-bottom:0.8rem;">${detail}</div>` : ''}
            <div class="fg"><input type="text" placeholder="Type your response..." id="${inputId}"></div>
            <button class="btn btn-p btn-sm" onclick="sendComplaintResponse(${c.complaintId}, '${inputId}')">📨 Send Response</button>
          </div>`;
        }).join('');
    }

    if(resolved.length === 0){
      resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No resolved complaints yet.</div>';
    } else {
      resolvedEl.innerHTML = resolved
        .slice()
        .sort((a,b) => (b.complaintId || 0) - (a.complaintId || 0))
        .map(c => {
          const lines = String(c.complaintText || '').split('\n');
          const subject = lines[0] || 'Complaint';
          return `<div style="opacity:0.85;border:1px solid var(--cream-dark);border-radius:14px;padding:1.2rem;margin-bottom:0.7rem;">
            <div style="color:var(--text);font-weight:600;">${escapeHtml(c.landOwnerName || 'Land Owner')} <span class="b g" style="font-size:0.68rem;margin-left:6px;">Resolved</span></div>
            <div style="font-size:0.8rem;color:var(--muted);margin-top:2px;">${subject} · ${c.dateSubmitted || '—'}</div>
            <div style="margin-top:6px;font-size:0.82rem;color:var(--muted);background:var(--cream);padding:0.45rem 0.7rem;border-radius:8px;"><strong>Factory response:</strong> ${escapeHtml(c.responseText || 'Resolved')}</div>
          </div>`;
        }).join('');
    }
  } catch(e){
    pendingEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</div>';
    resolvedEl.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">⚠️ Cannot reach server.</div>';
    console.warn('loadFactoryComplaints:', e.message);
  }
}

function sendResp(inputId,btn){
  toast('This handler is deprecated. Please use the live complaint cards.', true);
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
  if(amount >= 1000000) return `Rs ${(amount / 1000000).toFixed(2)}M`;
  if(amount >= 1000) return `Rs ${Math.round(amount / 1000)}k`;
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
  const diffMs = Math.max(0, Date.now() - dateObj.getTime());
  const mins = Math.floor(diffMs / 60000);
  if(mins < 60) return mins <= 1 ? 'just now' : `${mins} mins ago`;
  const hours = Math.floor(mins / 60);
  if(hours < 24) return hours === 1 ? '1 hour ago' : `${hours} hours ago`;
  const days = Math.floor(hours / 24);
  if(days < 7) return days === 1 ? '1 day ago' : `${days} days ago`;
  return dateObj.toLocaleDateString('en-GB', { year: 'numeric', month: 'short', day: '2-digit' });
}

function renderFactoryRecentActivity(items){
  const host = document.getElementById('dash-recent-activity');
  if(!host) return;

  if(!items.length){
    host.innerHTML = '<div class="ai"><div class="adot b">i</div><div class="at"><div class="am">No recent records yet</div><div class="ti">Activity will appear here</div></div></div>';
    return;
  }

  host.innerHTML = items.slice(0, 5).map(a => `
    <div class="ai">
      <div class="adot ${a.dotClass}">${a.icon}</div>
      <div class="at">
        <div class="am">${a.message}</div>
        <div class="ti">${agoLabel(a.when)}</div>
      </div>
    </div>
  `).join('');
}

function renderDashPendingLoans(pending){
  const host = document.getElementById('dash-pending-loan-list');
  if(!host) return;

  if(!pending.length){
    host.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No pending loan requests.</div>';
    return;
  }

  const cards = pending.slice(0, 3).map(l => `
    <div style="background:rgba(200,168,75,0.1);border:1px solid rgba(200,168,75,0.2);border-radius:11px;padding:0.9rem;">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <div><div style="color:var(--text);font-weight:600;font-size:0.85rem;">${escapeHtml(l.landOwnerName || 'Land Owner')}</div><div style="color:var(--muted);font-size:0.75rem;">${escapeHtml(l.reason || 'Loan request')}</div></div>
        <div style="font-family:'Playfair Display',serif;font-size:1.1rem;color:var(--amber);">${formatCurrency(l.loanAmount)}</div>
      </div>
      <div style="display:flex;gap:0.5rem;margin-top:0.7rem;">
        <button class="btn btn-a btn-sm" onclick="approveLoan(${l.loanId})">✓ Approve</button>
        <button class="btn btn-d btn-sm" onclick="rejectLoan(${l.loanId})">✗ Reject</button>
      </div>
    </div>
  `).join('');

  host.innerHTML = `${cards}<div style="text-align:center;"><button class="btn btn-o btn-sm" onclick="nav('loans',null)">View All ${pending.length} -></button></div>`;
}

function renderDashPendingFertilizer(pending){
  const host = document.getElementById('dash-pending-fert-list');
  if(!host) return;

  if(!pending.length){
    host.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No pending fertilizer requests.</div>';
    return;
  }

  const cards = pending.slice(0, 3).map(r => `
    <div style="background:rgba(61,107,79,0.1);border:1px solid rgba(61,107,79,0.2);border-radius:11px;padding:0.9rem;">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <div><div style="color:var(--text);font-weight:600;font-size:0.85rem;">${escapeHtml(r.landOwnerName || 'Land Owner')}</div><div style="color:var(--muted);font-size:0.75rem;">${toNumber(r.quantity).toLocaleString('en-IN')} kg - ${r.fertilizerType || '-'}</div></div>
        <div style="font-family:'Playfair Display',serif;font-size:1.1rem;color:var(--tea);">${formatCurrency(r.cost)}</div>
      </div>
      <div style="display:flex;gap:0.5rem;margin-top:0.7rem;">
        <button class="btn btn-a btn-sm" onclick="approveFert(${r.requestId})">✓ Approve</button>
        <button class="btn btn-d btn-sm" onclick="rejectFert(${r.requestId})">✗ Reject</button>
      </div>
    </div>
  `).join('');

  host.innerHTML = `${cards}<div style="text-align:center;"><button class="btn btn-o btn-sm" onclick="nav('fertilizer',null)">View All ${pending.length} -></button></div>`;
}

function renderDashTopDeliverers(deliveries, now){
  const host = document.getElementById('dash-top-deliverers');
  if(!host) return;

  const byOwner = new Map();
  deliveries.forEach(d => {
    const dDate = parseSafeDate(d.deliveryDate);
    if(!isSameMonth(dDate, now)) return;
    const key = String(d.landOwnerId || d.landOwnerName || 'owner');
    const entry = byOwner.get(key) || { name: d.landOwnerName || 'Land Owner', kg: 0 };
    entry.kg += toNumber(d.teaWeight);
    byOwner.set(key, entry);
  });

  const top = Array.from(byOwner.values()).sort((a, b) => b.kg - a.kg).slice(0, 3);
  if(!top.length){
    host.innerHTML = '<div style="text-align:center;color:var(--muted);padding:1rem;">No deliveries this month yet.</div>';
    return;
  }

  host.innerHTML = top.map((o, idx) => {
    const rankStyle = idx === 0
      ? 'background:var(--amber);color:var(--tea-dark);'
      : (idx === 1 ? 'background:var(--cream-dark);color:var(--text);' : 'background:var(--cream);color:var(--muted);');
    return `<div style="display:flex;justify-content:space-between;align-items:center;font-size:0.84rem;"><div style="display:flex;align-items:center;gap:6px;"><span style="${rankStyle}width:18px;height:18px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:0.65rem;font-weight:700;">${idx + 1}</span><span style="color:var(--text);">${escapeHtml(o.name)}</span></div><strong style="color:var(--tea);">${Math.round(o.kg).toLocaleString('en-IN')} kg</strong></div>`;
  }).join('');
}

async function loadFactoryDashboardOverview(){
  if(!currentFactoryOwnerId) return;

  try {
    const [deliveries, activeOwners, loans, fertilizers, cheques, pendingComplaints, allComplaints] = await Promise.all([
      fetchArrayOrEmpty(`${API}/api/deliveries/factory/${currentFactoryOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/factories/active-owners/${currentFactoryOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/loans/factory/${currentFactoryOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/fertilizer/factory/${currentFactoryOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/cheques/factory/${currentFactoryOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/complaints/pending/${currentFactoryOwnerId}`),
      fetchArrayOrEmpty(`${API}/api/complaints/factory/${currentFactoryOwnerId}`)
    ]);

    const now = new Date();
    const monthDeliveries = deliveries.filter(d => isSameMonth(parseSafeDate(d.deliveryDate), now));
    const totalKgMonth = monthDeliveries.reduce((sum, d) => sum + toNumber(d.teaWeight), 0);
    const totalPayableMonth = monthDeliveries.reduce((sum, d) => {
      const rate = toNumber(d.ratePerKg || currentRate);
      const amount = toNumber(d.totalAmount) || (toNumber(d.teaWeight) * rate);
      return sum + amount;
    }, 0);

    const pendingLoans = loans.filter(l => l.approvalStatus === 'Pending').sort((a, b) => new Date(b.requestDate) - new Date(a.requestDate));
    const pendingFert = fertilizers.filter(f => f.approvalStatus === 'Pending').sort((a, b) => (b.requestId || 0) - (a.requestId || 0));

    const resolvedComplaints = allComplaints.filter(c => c.status === 'Resolved');
    const openComplaints = pendingComplaints.length || allComplaints.filter(c => c.status === 'Pending').length;

    const ownersEl = document.getElementById('dash-active-owners');
    const deliveriesEl = document.getElementById('dash-total-deliveries');
    const payableEl = document.getElementById('dash-total-payable');
    const loansEl = document.getElementById('dash-pending-loans');
    const fertEl = document.getElementById('dash-pending-fertilizer');
    const compEl = document.getElementById('dash-open-complaints');
    if(ownersEl) ownersEl.textContent = String(activeOwners.length);
    if(deliveriesEl) deliveriesEl.textContent = Math.round(totalKgMonth).toLocaleString('en-IN');
    if(payableEl) payableEl.textContent = compactCurrency(totalPayableMonth);
    if(loansEl) loansEl.textContent = String(pendingLoans.length);
    if(fertEl) fertEl.textContent = String(pendingFert.length);
    if(compEl) compEl.textContent = String(openComplaints);

    const loanBadge = document.getElementById('loan-nav-badge');
    const fertBadge = document.getElementById('fert-nav-badge');
    const compBadge = document.getElementById('comp-nav-badge');
    if(loanBadge) loanBadge.textContent = String(pendingLoans.length);
    if(fertBadge) fertBadge.textContent = String(pendingFert.length);
    if(compBadge) compBadge.textContent = String(openComplaints);

    const activity = [];
    deliveries.forEach(d => {
      const when = parseSafeDate(d.deliveryDate);
      if(!when) return;
      activity.push({
        when,
        icon: '🍃',
        dotClass: 'g',
        message: `${escapeHtml(d.landOwnerName || 'Land Owner')} delivered ${toNumber(d.teaWeight).toLocaleString('en-IN')} kg`
      });
    });
    loans.forEach(l => {
      const when = parseSafeDate(l.requestDate);
      if(!when) return;
      activity.push({
        when,
        icon: '📋',
        dotClass: 'y',
        message: `Loan request ${formatCurrency(l.loanAmount)} by ${escapeHtml(l.landOwnerName || 'Land Owner')}`
      });
    });
    fertilizers.forEach(f => {
      const when = parseSafeDate(f.requestDate || f.requestedDate);
      if(!when) return;
      activity.push({
        when,
        icon: '🌿',
        dotClass: 'y',
        message: `Fertilizer ${toNumber(f.quantity).toLocaleString('en-IN')} kg request - ${escapeHtml(f.landOwnerName || 'Land Owner')}`
      });
    });
    cheques.forEach(c => {
      const when = parseSafeDate(c.issueDate || c.createdDate || c.date);
      if(!when) return;
      activity.push({
        when,
        icon: '💳',
        dotClass: 'b',
        message: `CHQ-${String(c.chequeId || '').padStart(4, '0')} issued to ${escapeHtml(c.landOwnerName || 'Land Owner')}`
      });
    });
    allComplaints.forEach(c => {
      const when = parseSafeDate(c.dateSubmitted);
      if(!when) return;
      activity.push({
        when,
        icon: '📣',
        dotClass: c.status === 'Resolved' ? 'g' : 'r',
        message: `Complaint ${c.status === 'Resolved' ? 'resolved' : 'filed'} by ${escapeHtml(c.landOwnerName || 'Land Owner')}`
      });
    });

    activity.sort((a, b) => b.when - a.when);
    renderFactoryRecentActivity(activity);
    renderDashPendingLoans(pendingLoans);
    renderDashPendingFertilizer(pendingFert);
    renderDashTopDeliverers(deliveries, now);
    buildCharts(deliveries);
  } catch(e){
    console.warn('loadFactoryDashboardOverview:', e.message);
  }
}

let currentRate    = 50;
let delRecordCount = 0;


async function loadCurrentRate(){
  if(!currentFactoryOwnerId) return;
  try {
    const res = await TeaGo.request(`${API}/api/deliveries/rate/${currentFactoryOwnerId}`);
    if(!res.ok) return;
    const data = await res.json();
    const rate = parseFloat(data.ratePerKg ?? 50);
    currentRate = rate;
    document.getElementById('rate-val').value               = rate;
    document.getElementById('rate-display').textContent     = 'Rs ' + rate + '/kg';
    document.getElementById('rate-cur-lbl').textContent     = 'Rs ' + rate + '/kg';
  } catch(e){
    console.warn('loadCurrentRate:', e.message);
  }
}


async function setRate(){
  const rate = document.getElementById('rate-val').value;
  const effectiveFrom = document.getElementById('rate-date').value;

  if(!rate || parseFloat(rate) <= 0){ toast('Please enter a valid rate.', true); return; }
  if(!currentFactoryOwnerId){ toast('No factory session found. Please sign in again.', true); return; }

  const btn = document.querySelector('[onclick="setRate()"]');
  btn.disabled    = true;
  btn.textContent = '⏳ Saving…';

  try {
    const res = await TeaGo.request(`${API}/api/deliveries/rate`, {
      method : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body   : JSON.stringify({
        factoryOwnerId : currentFactoryOwnerId,
        ratePerKg      : parseFloat(rate),
        effectiveFrom  : effectiveFrom || new Date().toISOString().split('T')[0]
      })
    });

    const data = await res.json();

    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to save rate'), true);
      return;
    }

    
    const savedRate = parseFloat(data.ratePerKg ?? rate);
    currentRate = savedRate;
    document.getElementById('rate-display').textContent = 'Rs ' + savedRate + '/kg';
    document.getElementById('rate-cur-lbl').textContent = 'Rs ' + savedRate + '/kg';
    toast('✓ Tea rate updated to Rs ' + savedRate + '/kg — saved to database');
    loadFactoryDashboardOverview();

  } catch(e){
    toast('❌ Network error — is the server running?', true);
    console.error(e);
  } finally {
    btn.disabled    = false;
    btn.textContent = '💰 Update Rate';
  }
}


function calcFactoryDel(){
  const w   = parseFloat(document.getElementById('del-weight').value) || 0;
  const deduction = parseFloat(document.getElementById('del-deduction').value) || 0;
  const grossAmt = w * currentRate;
  const netAmt = grossAmt - deduction;
  
  document.getElementById('del-amount').value = w ? 'Rs ' + netAmt.toLocaleString() : '';
  
  const preview   = document.getElementById('del-preview');
  const sel       = document.getElementById('del-owner');
  const ownerName = sel.options[sel.selectedIndex].text.split(' (')[0];
  
  if(w){
    preview.style.display = 'block';
    document.getElementById('del-preview-text').textContent = ownerName + ' · ' + w + ' kg × Rs ' + currentRate;
    document.getElementById('del-preview-gross').textContent = 'Rs ' + grossAmt.toLocaleString();
    
    const deductionRow = document.getElementById('del-preview-deduction-row');
    if(deduction > 0){
      deductionRow.style.display = 'flex';
      document.getElementById('del-preview-deduction').textContent = 'Rs ' + deduction.toLocaleString();
    } else {
      deductionRow.style.display = 'none';
    }
    
    document.getElementById('del-preview-amt').textContent = 'Rs ' + netAmt.toLocaleString();
  } else {
    preview.style.display = 'none';
  }
}

function updateOwnerBadge(){ calcFactoryDel(); }


async function saveFactoryDelivery(){
  const sel         = document.getElementById('del-owner');
  const landOwnerId = parseInt(sel.value);


  if (!sel.value || isNaN(landOwnerId)) {
    toast('Please select a land owner.', true);
    return;
  }

  const ownerName = sel.options[sel.selectedIndex].text.split(' (')[0];
  const date      = document.getElementById('del-date').value;
  const weight    = parseFloat(document.getElementById('del-weight').value);
  const deduction = parseFloat(document.getElementById('del-deduction').value) || 0;

  if(!date)                { toast('Please select a delivery date.', true); return; }
  if(!weight || weight<=0) { toast('Please enter a valid weight.',   true); return; }

  const btn = document.querySelector('[onclick="saveFactoryDelivery()"]');
  btn.disabled    = true;
  btn.textContent = '⏳ Saving…';

  try {
    const res = await TeaGo.request(`${API}/api/deliveries`, {
      method : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body   : JSON.stringify({
        factoryOwnerId : currentFactoryOwnerId,
        landOwnerId    : landOwnerId,
        deliveryDate   : date,
        teaWeight      : weight,
        deductionAmount: deduction
        
      })
    });

    const data = await res.json();

    if(!res.ok){
      toast('❌ ' + (data.message || 'Failed to record delivery'), true);
      return;
    }

   
    appendDeliveryRow(data);
    delRecordCount++;
    document.getElementById('del-count').textContent = delRecordCount + ' records';

   
    loadAllDeliveries();
    loadFactoryDashboardOverview();

    
    document.getElementById('del-weight').value           = '';
    document.getElementById('del-deduction').value        = '';
    document.getElementById('del-amount').value           = '';
    document.getElementById('del-preview').style.display = 'none';

    const fmtAmt = 'Rs ' + parseFloat(data.totalAmount).toLocaleString();
    toast('✓ Delivery of ' + data.teaWeight + ' kg recorded for ' + (data.landOwnerName || ownerName) + ' — ' + fmtAmt);

  } catch(e){
    toast('❌ Network error — is the server running?', true);
    console.error(e);
  } finally {
    btn.disabled    = false;
    btn.textContent = '🍃 Record Delivery';
  }
}


async function loadAllDeliveries(){
  const tbody    = document.getElementById('del-records-tb');
  const countLbl = document.getElementById('del-count');

  tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1.5rem;">
      ⏳ Loading deliveries…</td></tr>`;

  if(!currentFactoryOwnerId){
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1.5rem;">
        No factory session found. Please sign in again.</td></tr>`;
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/deliveries/factory/${currentFactoryOwnerId}`);

    if(!res.ok){
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--danger);padding:1.5rem;">
          ❌ Could not load deliveries (${res.status})</td></tr>`;
      updateDeliveryStats([], 0);
      return;
    }

    const deliveries = await res.json();
    tbody.innerHTML  = '';

    if(!Array.isArray(deliveries) || deliveries.length === 0){
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1.5rem;">
          No deliveries recorded yet.</td></tr>`;
      delRecordCount = 0;
      countLbl.textContent = '0 records';
      updateDeliveryStats([], 0);
      return;
    }

    
    deliveries
      .slice()
      .sort((a, b) => new Date(b.deliveryDate) - new Date(a.deliveryDate))
      .forEach(d => appendDeliveryRow(d));

    delRecordCount = deliveries.length;
    countLbl.textContent = delRecordCount + ' records';

    updateDeliveryStats(deliveries, currentRate);

  } catch(e){
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--muted);padding:1.5rem;">
        ⚠️ Cannot reach server. Showing offline view.</td></tr>`;
    console.warn('loadAllDeliveries:', e.message);
  }
}


function updateDeliveryStats(deliveries, rate){
  const todayStr = new Date().toISOString().split('T')[0];   
  const now      = new Date();
  const curMonth = now.getMonth() + 1;   
  const curYear  = now.getFullYear();

  
  const monthKg = deliveries
    .filter(d => {
     
      const m = d.month ?? new Date(d.deliveryDate).getMonth() + 1;
      const y = d.year  ?? new Date(d.deliveryDate).getFullYear();
      return m === curMonth && y === curYear;
    })
    .reduce((sum, d) => sum + parseFloat(d.teaWeight || 0), 0);


  const todayKg = deliveries
    .filter(d => String(d.deliveryDate).startsWith(todayStr))
    .reduce((sum, d) => sum + parseFloat(d.teaWeight || 0), 0);


  const activeOwners = new Set(deliveries.map(d => d.landOwnerId)).size;

  const monthEl  = document.getElementById('del-stat-month');
  const todayEl  = document.getElementById('del-stat-today');
  const ownersEl = document.getElementById('del-stat-owners');

  if(monthEl)  monthEl.textContent  = monthKg.toLocaleString() + ' kg';
  if(todayEl)  todayEl.textContent  = todayKg.toLocaleString() + ' kg';
  if(ownersEl) ownersEl.textContent = String(activeOwners);
}


function appendDeliveryRow(d){
  const tbody   = document.getElementById('del-records-tb');
  const rate    = parseFloat(d.ratePerKg   ?? currentRate);
  const deduction = parseFloat(d.deductionAmount ?? 0);
  const grossAmt   = parseFloat(d.teaWeight) * rate;
  const netAmt  = grossAmt - deduction;
  const fmtGross  = 'Rs ' + grossAmt.toLocaleString();
  const fmtDeduction = 'Rs ' + deduction.toLocaleString();
  const fmtNet  = 'Rs ' + netAmt.toLocaleString();
  
  const deductionStyle = deduction > 0 ? 'color:var(--danger);font-weight:600;' : '';
  
  tbody.insertAdjacentHTML('afterbegin',
    `<tr style="animation:rise 0.3s ease;">
      <td>${d.deliveryDate}</td>
      <td><strong>${escapeHtml(d.landOwnerName)}</strong></td>
      <td>${d.teaWeight}</td>
      <td>Rs ${rate}</td>
      <td>${fmtGross}</td>
      <td style="${deductionStyle}">${fmtDeduction}</td>
      <td><strong style="color:var(--tea)">${fmtNet}</strong></td>
      <td><span class="b g">Recorded</span></td>
    </tr>`);
}


async function signOut(){
  try { await TeaGo.logout(); } catch (error) { toast(error.message, true); }
}


async function populateOwnerDropdown(){
  const sel = document.getElementById('del-owner');
  if(!sel) return;

  if(!currentFactoryOwnerId){
    sel.innerHTML = '<option value="" disabled selected>No factory session</option>';
    return;
  }

  try {
    const res = await TeaGo.request(`${API}/api/factories/active-owners/${currentFactoryOwnerId}`);

    if(!res.ok){
      sel.innerHTML = '<option value="" disabled selected>❌ Could not load owners</option>';
      return;
    }

    const owners = await res.json();

    if(!Array.isArray(owners) || owners.length === 0){
      sel.innerHTML = '<option value="" disabled selected>No approved land owners yet</option>';
      return;
    }

    
    sel.innerHTML = owners
      .map(o => `<option value="${o.landOwnerId}">${escapeHtml(o.displayName)}</option>`)
      .join('');

    
    calcFactoryDel();

  } catch(e){
    sel.innerHTML = '<option value="" disabled selected>⚠️ Server unreachable</option>';
    console.warn('populateOwnerDropdown:', e.message);
  }
}

function buildCharts(deliveries){
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
  document.getElementById('fac-chart').innerHTML = series.map((s, i) =>
    `<div class="bc"><div class="bar ${i===series.length-1?'hi':''}" style="height:${Math.max(6, Math.round((s.value / max) * 100))}px"></div><div class="blbl">${s.label}</div></div>`).join('');

  const daily=[3100,2800,3400,3200,3600,3240];
  const dm=Math.max(...daily);
  const dc=document.getElementById('daily-chart');
  if(dc)dc.innerHTML=['Mon','Tue','Wed','Thu','Fri','Sat'].map((m,i)=>
    `<div class="bc"><div class="bar ${i===4?'hi':''}" style="height:${Math.round(daily[i]/dm*100)}px"></div><div class="blbl">${m}</div></div>`).join('');
}

async function viewLandOwnerDetails(landOwnerId, ownerName){
  console.log('viewLandOwnerDetails called:', landOwnerId, ownerName);
  
  if(!currentFactoryOwnerId){
    toast('No factory session found.', true);
    return;
  }

  const contentEl = document.getElementById('details-content');
  const titleEl = document.getElementById('details-owner-name');

  if(!contentEl || !titleEl){
    toast('Modal elements not found!', true);
    console.error('Modal elements missing');
    return;
  }

  titleEl.textContent = (ownerName || 'Land Owner') + ' — Details';
  contentEl.innerHTML = '<div style="text-align:center;color:var(--muted);">⏳ Loading details...</div>';
  openM('m-owner-details');

  try {
    const apiUrl = `${API}/api/factories/${currentFactoryOwnerId}/owner-details/${landOwnerId}`;
    console.log('Fetching from:', apiUrl);
    
    const res = await TeaGo.request(apiUrl);
    const data = await res.json();

    console.log('Response status:', res.status);
    console.log('Response data:', data);

    if(!res.ok){
      contentEl.innerHTML = `<div style="color:var(--danger);padding:1rem;background:rgba(214,64,69,0.1);border-radius:10px;"><strong>Error:</strong> ${data.message || 'Could not load details'}</div>`;
      return;
    }

    const statusColor = data.status === 'Active' ? 'g' : (data.status === 'Rejected' ? 'r' : 'y');
    const registrationDate = data.registrationDate ? new Date(data.registrationDate).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';
    const dob = data.dateOfBirth ? new Date(data.dateOfBirth).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';

    contentEl.innerHTML = `
      <div>
        <div style="font-size:0.75rem;text-transform:uppercase;letter-spacing:0.06em;color:var(--muted);margin-bottom:0.5rem;margin-top:0;">Personal Information</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;background:rgba(61,107,79,0.08);border:1px solid rgba(61,107,79,0.15);border-radius:10px;padding:0.9rem;">
          <div><span style="color:var(--muted);font-size:0.75rem;">Full Name</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${escapeHtml(data.fullName || data.name || '—')}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Name with Initials</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${data.nameWithInitials || '—'}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">NIC</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${escapeHtml(data.nic || '—')}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Contact Number</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${escapeHtml(data.contactNumber || '—')}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Date of Birth</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${dob}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Address</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${escapeHtml(data.address || '—')}</div></div>
        </div>
      </div>

      <div>
        <div style="font-size:0.75rem;text-transform:uppercase;letter-spacing:0.06em;color:var(--muted);margin-bottom:0.5rem;margin-top:0;">Land Details</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;background:rgba(200,168,75,0.08);border:1px solid rgba(200,168,75,0.15);border-radius:10px;padding:0.9rem;">
          <div><span style="color:var(--muted);font-size:0.75rem;">Total Land Size</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${data.totalLandSize ? Number(data.totalLandSize).toFixed(2) : '—'} acres</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Land Size (This Factory)</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${data.factoryLandSize ? Number(data.factoryLandSize).toFixed(2) : '—'} acres</div></div>
          <div style="grid-column:1/-1;"><span style="color:var(--muted);font-size:0.75rem;">Land Location</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${escapeHtml(data.landLocation || '—')}</div></div>
        </div>
      </div>

      <div>
        <div style="font-size:0.75rem;text-transform:uppercase;letter-spacing:0.06em;color:var(--muted);margin-bottom:0.5rem;margin-top:0;">Bank Details</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;background:rgba(61,107,79,0.08);border:1px solid rgba(61,107,79,0.15);border-radius:10px;padding:0.9rem;">
          <div><span style="color:var(--muted);font-size:0.75rem;">Account Name</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${escapeHtml(data.bankAccountName || '—')}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Bank Name</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${escapeHtml(data.bankName || '—')}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Branch</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${data.bankBranch || '—'}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Account Number</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${data.bankAccountNumber || '—'}</div></div>
        </div>
      </div>

      <div>
        <div style="font-size:0.75rem;text-transform:uppercase;letter-spacing:0.06em;color:var(--muted);margin-bottom:0.5rem;margin-top:0;">Emergency Contact</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;background:rgba(214,64,69,0.08);border:1px solid rgba(214,64,69,0.15);border-radius:10px;padding:0.9rem;">
          <div><span style="color:var(--muted);font-size:0.75rem;">Name</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${escapeHtml(data.emergencyContactName || '—')}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Relationship</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${data.emergencyContactRelationship || '—'}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Phone</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${data.emergencyContactPhone || '—'}</div></div>
          <div style="grid-column:1/-1;"><span style="color:var(--muted);font-size:0.75rem;">Address</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${data.emergencyContactAddress || '—'}</div></div>
        </div>
      </div>

      <div>
        <div style="font-size:0.75rem;text-transform:uppercase;letter-spacing:0.06em;color:var(--muted);margin-bottom:0.5rem;margin-top:0;">Factory Registration</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;background:rgba(200,168,75,0.08);border:1px solid rgba(200,168,75,0.15);border-radius:10px;padding:0.9rem;">
          <div><span style="color:var(--muted);font-size:0.75rem;">Registration Date</span><div style="font-weight:600;color:var(--text);margin-top:0.3rem;">${registrationDate}</div></div>
          <div><span style="color:var(--muted);font-size:0.75rem;">Status</span><div style="font-weight:600;margin-top:0.3rem;"><span class="b ${statusColor}">${data.status || '—'}</span></div></div>
        </div>
      </div>
    `;
  } catch(e){
    console.error('viewLandOwnerDetails error:', e);
    contentEl.innerHTML = '<div style="color:var(--danger);padding:1rem;background:rgba(214,64,69,0.1);border-radius:10px;"><strong>Error:</strong> Cannot reach server - ' + e.message + '</div>';
  }
}

function toast(msg,err=false){
  const t=document.getElementById('toast');
  t.textContent=msg;t.style.display='block';
  t.style.background=err?'var(--danger)':'var(--ink-mid)';
  clearTimeout(t._t);t._t=setTimeout(()=>t.style.display='none',3000);
}
