<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>

<t:layout title="Packing Wizard">
    <jsp:attribute name="actions">
        <span class="badge badge-primary py-2 px-3 shadow-sm">
            <i class="fas fa-layer-group mr-1"></i> Step ${step} of 3
        </span>
    </jsp:attribute>

    <jsp:body>
        <!-- Stepper UI -->
        <div class="card shadow mb-4 border-0 bg-gradient-primary text-white overflow-hidden" style="border-radius: 15px;">
            <div class="card-body py-4 position-relative" style="z-index: 1;">
                <div class="row no-gutters align-items-center justify-content-center text-center">
                    <div class="col">
                        <div class="step-item ${step >= 1 ? 'active' : ''}">
                            <div class="step-icon mb-2 mx-auto shadow-lg">1</div>
                            <div class="small font-weight-bold">Select GDN</div>
                        </div>
                    </div>
                    <div class="col-auto px-0"><div class="step-connector ${step >= 2 ? 'active' : ''}"></div></div>
                    <div class="col">
                        <div class="step-item ${step >= 2 ? 'active' : ''}">
                            <div class="step-icon mb-2 mx-auto ${step >= 2 ? 'shadow-lg' : ''}">2</div>
                            <div class="small font-weight-bold">Config Lines</div>
                        </div>
                    </div>
                    <div class="col-auto px-0"><div class="step-connector ${step >= 3 ? 'active' : ''}"></div></div>
                    <div class="col">
                        <div class="step-item ${step >= 3 ? 'active' : ''}">
                            <div class="step-icon mb-2 mx-auto ${step >= 3 ? 'shadow-lg' : ''}">3</div>
                            <div class="small font-weight-bold">Assign Staff</div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Decorative circle -->
            <div class="position-absolute" style="width: 200px; height: 200px; background: rgba(255,255,255,0.1); border-radius: 50%; top: -100px; right: -50px; z-index: 0;"></div>
        </div>

        <c:choose>
            <%-- STEP 1: Select GDN --%>
            <c:when test="${step == 1}">
                <div class="card shadow border-0" style="border-radius: 15px;">
                    <div class="card-body p-4 p-md-5">
                        <form action="${pageContext.request.contextPath}/packing" method="get" id="formStep1">
                            <input type="hidden" name="action" value="create"/>
                            <input type="hidden" name="step" value="2"/>
                            
                            <div class="text-center mb-5">
                                <div class="bg-light d-inline-block rounded-circle p-4 mb-3">
                                    <i class="fas fa-file-invoice fa-3x text-primary"></i>
                                </div>
                                <h4 class="font-weight-bold text-gray-800">Identify Goods Delivery Note</h4>
                                <p class="text-muted">Choose a picking-completed GDN to start the packing process.</p>
                            </div>

                            <div class="row justify-content-center mb-4">
                                <div class="col-lg-8">
                                    <div class="form-group position-relative">
                                        <label class="font-weight-bold text-gray-700 ml-1">Search or Select GDN</label>
                                        <div class="input-group input-group-lg shadow-sm rounded-lg overflow-hidden border">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text bg-white border-0"><i class="fas fa-search text-muted"></i></span>
                                            </div>
                                            <select name="gdnId" id="gdnSelect" class="form-control border-0" required onchange="loadGdnDetail(this.value)">
                                                <option value="">-- Start typing GDN number... --</option>
                                                <c:forEach var="r" items="${readyGdns}">
                                                    <option value="${r.gdnId}">${r.gdnNumber} (SO: ${r.soNumber} - Client: ${r.customerName})</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <c:if test="${empty readyGdns}">
                                            <div class="alert alert-warning mt-3 border-0 shadow-sm rounded-lg">
                                                <i class="fas fa-exclamation-circle mr-2"></i> No GDNs are currently in <strong>PACKING</strong> status and unassigned.
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- GDN Detail Preview -->
                            <div id="gdnPreview" class="d-none row justify-content-center">
                                <div class="col-lg-8">
                                    <div class="card bg-gray-100 border-0 shadow-sm rounded-lg mb-4">
                                        <div class="card-body py-3">
                                            <div class="row text-center small">
                                                <div class="col border-right border-gray-300">
                                                    <div class="text-muted text-uppercase mb-1" style="font-size: 0.65rem">Customer</div>
                                                    <div class="font-weight-bold text-gray-800" id="previewCustomer">-</div>
                                                </div>
                                                <div class="col border-right border-gray-300">
                                                    <div class="text-muted text-uppercase mb-1" style="font-size: 0.65rem">Sales Order</div>
                                                    <div class="font-weight-bold text-gray-800" id="previewSo">-</div>
                                                </div>
                                                <div class="col">
                                                    <div class="text-muted text-uppercase mb-1" style="font-size: 0.65rem">Total Lines</div>
                                                    <div class="font-weight-bold text-gray-800" id="previewLines">-</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="d-flex justify-content-center mt-4">
                                <a href="${pageContext.request.contextPath}/packing?action=list" class="btn btn-light px-5 mr-3 rounded-pill h6 mb-0 py-3 font-weight-bold">Cancel</a>
                                <button type="submit" class="btn btn-primary px-5 shadow rounded-pill h6 mb-0 py-3 font-weight-bold" id="btnNext1" disabled>
                                    Initialize Configuration <i class="fas fa-arrow-right ml-2"></i>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
                
                <script>
                    function loadGdnDetail(gdnId) {
                        const btn = document.getElementById('btnNext1');
                        const preview = document.getElementById('gdnPreview');
                        if(!gdnId) {
                            btn.disabled = true;
                            preview.classList.add('d-none');
                            return;
                        }
                        btn.disabled = false;
                        fetch(`${pageContext.request.contextPath}/packing?action=getGdnDetail&gdnId=` + gdnId)
                            .then(response => response.json())
                            .then(data => {
                                if(data && data.gdnId) {
                                    document.getElementById('previewCustomer').textContent = data.customerName || '-';
                                    document.getElementById('previewSo').textContent = data.soNumber || '-';
                                    document.getElementById('previewLines').textContent = (data.lines ? data.lines.length : 0);
                                    preview.classList.remove('d-none');
                                }
                            });
                    }
                </script>
            </c:when>
            
            <%-- STEP 2: Configure Lines --%>
            <c:when test="${step == 2}">
                <div class="card shadow border-0 mb-4" style="border-radius: 15px;">
                    <div class="card-header bg-white py-4 d-flex align-items-center border-0">
                        <div class="icon-circle bg-light mr-3"><i class="fas fa-boxes text-primary"></i></div>
                        <div>
                            <h5 class="m-0 font-weight-bold text-gray-800">Configure Packaging per Line</h5>
                            <div class="small text-muted font-weight-bold">
                                GDN: <span class="text-primary">${gdn.gdnNumber}</span> | ${gdn.customerName}
                            </div>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        <form action="${pageContext.request.contextPath}/packing" method="get" id="formStep2">
                            <input type="hidden" name="action" value="create"/>
                            <input type="hidden" name="step" value="3"/>
                            <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>
                            
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="bg-light text-muted small text-uppercase font-weight-bold">
                                        <tr>
                                            <th class="px-4 py-3">Product Information</th>
                                            <th class="text-center">Qty Picked</th>
                                            <th class="text-center" style="width: 250px">Items per Pack</th>
                                            <th class="text-center" style="width: 200px">Calculated Packs</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="line" items="${gdn.lines}">
                                            <tr>
                                                <td class="px-4">
                                                    <div class="font-weight-bold text-gray-800">${line.variantSku}</div>
                                                    <div class="small text-muted">${line.productName}</div>
                                                    <div class="small">
                                                        <span class="badge badge-light border">${line.color} / ${line.size}</span>
                                                    </div>
                                                    <input type="hidden" name="gdnLineIds" value="${line.gdnLineId}"/>
                                                </td>
                                                <td class="text-center h4 font-weight-bold mb-0 text-gray-900 border-left border-right">
                                                    <fmt:formatNumber value="${line.qtyPicked}" maxFractionDigits="0"/>
                                                    <input type="hidden" id="qty_${line.gdnLineId}" value="${line.qtyPicked}"/>
                                                </td>
                                                <td class="px-4">
                                                    <div class="input-group input-group-lg shadow-sm border rounded">
                                                        <input type="number" name="itemsPerPack_${line.gdnLineId}" id="ipp_${line.gdnLineId}"
                                                               class="form-control border-0 text-center font-weight-bold" min="1" required 
                                                               oninput="calculatePacks(${line.gdnLineId})" value="1"/>
                                                        <div class="input-group-append">
                                                            <span class="input-group-text bg-white border-0 small text-muted">items/pk</span>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td class="px-4 text-center">
                                                    <div class="bg-gray-100 p-2 rounded-lg border shadow-sm">
                                                        <input type="hidden" name="numPacks_${line.gdnLineId}" id="np_${line.gdnLineId}" value="${line.qtyPicked}"/>
                                                        <span class="h4 font-weight-bold text-primary mb-0" id="np_display_${line.gdnLineId}">
                                                            <fmt:formatNumber value="${line.qtyPicked}" maxFractionDigits="0"/>
                                                        </span>
                                                        <div class="text-xs text-uppercase font-weight-bold text-muted">Packs Needed</div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                            
                            <div class="card-footer bg-white py-4 d-flex justify-content-between border-0">
                                <a href="${pageContext.request.contextPath}/packing?action=create&step=1" class="btn btn-light px-4 py-2 font-weight-bold shadow-sm">
                                    <i class="fas fa-arrow-left mr-2"></i> Previous
                                </a>
                                <button type="submit" class="btn btn-primary px-5 py-2 font-weight-bold shadow rounded-pill">
                                    Assign Personnel <i class="fas fa-arrow-right ml-2"></i>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
                
                <script>
                    function calculatePacks(lineId) {
                        const qtyPicked = parseFloat(document.getElementById('qty_' + lineId).value || 0);
                        let itemsPerPack = parseInt(document.getElementById('ipp_' + lineId).value);
                        if (isNaN(itemsPerPack) || itemsPerPack < 1) itemsPerPack = 1;
                        const numPacks = Math.ceil(qtyPicked / itemsPerPack);
                        document.getElementById('np_' + lineId).value = numPacks;
                        document.getElementById('np_display_' + lineId).textContent = numPacks;
                    }
                    
                    // Initial calc
                    document.addEventListener('DOMContentLoaded', () => {
                        document.querySelectorAll('input[id^="ipp_"]').forEach(el => {
                            const lineId = el.id.split('_')[1];
                            calculatePacks(lineId);
                        });
                    });
                </script>
            </c:when>

            <%-- STEP 3: Assign Staff --%>
            <c:when test="${step == 3}">
                <div class="row align-items-stretch mb-4">
                    <div class="col-lg-8">
                        <div class="alert alert-primary shadow-sm border-0 py-3 mb-0 h-100 rounded-lg d-flex align-items-center">
                            <i class="fas fa-user-tag fa-2x mr-4 opacity-50"></i>
                            <div>
                                <h6 class="alert-heading font-weight-bold mb-1">Finalize Assignments</h6>
                                <p class="small mb-0">Distribute the required packs across your warehouse staff. Each line must be fully assigned before submission.</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card shadow-sm border-0 h-100 rounded-lg bg-gray-800 text-white">
                            <div class="card-body py-3 d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="text-xs font-weight-bold text-uppercase mb-1" style="color:rgba(255,255,255,0.6)">GDN Reference</div>
                                    <div class="font-weight-bold h5 mb-0">${gdn.gdnNumber}</div>
                                </div>
                                <div class="text-right">
                                    <div class="text-xs font-weight-bold text-uppercase mb-1" style="color:rgba(255,255,255,0.6)">Lines</div>
                                    <div class="font-weight-bold h5 mb-0">${fn:length(gdn.lines)}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <form action="${pageContext.request.contextPath}/packing" method="post" id="formStep3" onsubmit="return validateAllPacks()">
                    <input type="hidden" name="action" value="submit"/>
                    <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>
                    
                    <c:forEach var="line" items="${gdn.lines}">
                        <c:set var="configVar" value="config_${line.gdnLineId}"/>
                        <c:set var="cfg" value="${requestScope[configVar]}"/>
                        
                        <input type="hidden" name="gdnLineIds" value="${line.gdnLineId}"/>
                        <input type="hidden" name="itemsPerPack_${line.gdnLineId}" value="${cfg.itemsPerPack}"/>
                        <input type="hidden" name="numPacks_${line.gdnLineId}" value="${cfg.numPacks}"/>

                        <div class="card shadow border-0 mb-4 line-card overflow-hidden" id="lineCard_${line.gdnLineId}" 
                             data-line-id="${line.gdnLineId}" data-target-packs="${cfg.numPacks}" style="border-radius: 15px;">
                            <div class="card-header bg-white py-3 border-0 d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center">
                                    <div class="line-badge rounded-lg mr-3 shadow-sm border bg-light text-center" style="width: 50px; height: 50px; border-radius: 12px!important;">
                                        <div class="text-muted" style="font-size: 0.6rem; margin-top: 5px;">LINE</div>
                                        <div class="font-weight-bold h5 mb-0 text-primary">#${line.gdnLineId}</div>
                                    </div>
                                    <div>
                                        <h6 class="m-0 font-weight-bold text-gray-800">${line.variantSku}</h6>
                                        <div class="small text-muted font-weight-bold">${cfg.itemsPerPack} items/pack × ${cfg.numPacks} packs</div>
                                    </div>
                                </div>
                                <div class="text-right d-none d-sm-block">
                                    <div class="small text-muted text-uppercase mb-1" style="font-size: 0.65rem">Required Packs</div>
                                    <div class="h5 font-weight-bold text-gray-900 mb-0">
                                        <span id="targetCount_${line.gdnLineId}">${cfg.numPacks}</span> 
                                        <i class="fas fa-boxes text-primary ml-1 fa-sm"></i>
                                    </div>
                                </div>
                            </div>
                            <div class="card-body py-0">
                                <div class="table-responsive">
                                    <table class="table table-borderless align-middle mb-0" id="taskTable_${line.gdnLineId}">
                                        <thead class="bg-gray-100 text-muted small text-uppercase py-2" style="border-radius: 8px;">
                                            <tr>
                                                <th class="py-2 pl-3">Staff Assignment</th>
                                                <th class="py-2 text-center" style="width: 250px">Packs to Handle</th>
                                                <th class="py-2 text-right pr-3" style="width: 100px">Remove</th>
                                            </tr>
                                        </thead>
                                        <tbody id="taskBody_${line.gdnLineId}">
                                            <tr class="task-row">
                                                <td class="pl-3 py-3">
                                                    <select name="taskUser_${line.gdnLineId}" class="form-control user-select shadow-sm" required>
                                                        <option value="">-- Choose Personnel --</option>
                                                        <c:forEach var="u" items="${staffList}">
                                                            <option value="${u.userId}">${u.fullName} (@${u.username})</option>
                                                        </c:forEach>
                                                    </select>
                                                </td>
                                                <td class="py-3">
                                                    <div class="input-group input-group-lg shadow-sm border rounded">
                                                        <input type="number" name="taskPacks_${line.gdnLineId}" class="form-control border-0 text-center font-weight-bold pack-input" 
                                                               min="1" max="${cfg.numPacks}" value="${cfg.numPacks}" required oninput="validateLinePacks(${line.gdnLineId})"/>
                                                        <div class="input-group-append">
                                                                <span class="input-group-text bg-white border-0 small text-muted">packs</span>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td class="text-right pr-3 py-3">
                                                    <button type="button" class="btn btn-outline-danger btn-sm border-0 rounded-circle delete-row" onclick="deleteRow(this, ${line.gdnLineId})" disabled style="width: 35px; height: 35px;">
                                                        <i class="fas fa-trash"></i>
                                                    </button>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="card-footer bg-gray-100 py-3 border-0 d-flex justify-content-between align-items-center">
                                <button type="button" class="btn btn-link py-0 font-weight-bold text-decoration-none" onclick="addRow(${line.gdnLineId})">
                                    <i class="fas fa-plus-circle mr-1"></i> Add Split Task
                                </button>
                                <div class="font-weight-bold">
                                    Status: <span id="currentTotal_${line.gdnLineId}" class="text-success font-weight-bold">${cfg.numPacks}</span> / ${cfg.numPacks}
                                    <span id="validIcon_${line.gdnLineId}" class="ml-2 text-success"><i class="fas fa-check-circle"></i></span>
                                </div>
                            </div>
                        </div>
                    </c:forEach>

                    <div class="sticky-footer bg-white shadow-lg p-4 rounded-lg d-flex justify-content-between align-items-center mb-5 border">
                        <a href="${pageContext.request.contextPath}/packing?action=create&step=2&gdnId=${gdn.gdnId}" class="btn btn-light px-4 font-weight-bold rounded-pill">
                            <i class="fas fa-undo-alt mr-2"></i> Modify Setup
                        </a>
                        <button type="submit" class="btn btn-success btn-lg px-5 font-weight-bold shadow-lg rounded-pill">
                            <i class="fas fa-rocket mr-2"></i> Launch Packing Plan
                        </button>
                    </div>
                </form>
                
                <!-- Template for new row -->
                <template id="rowTemplate">
                    <tr class="task-row">
                        <td class="pl-3 py-3">
                            <select class="form-control user-select shadow-sm" required>
                                <option value="">-- Choose Personnel --</option>
                                <c:forEach var="u" items="${staffList}">
                                    <option value="${u.userId}">${u.fullName} (@${u.username})</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td class="py-3">
                            <div class="input-group input-group-lg shadow-sm border rounded">
                                <input type="number" class="form-control border-0 text-center font-weight-bold pack-input" min="1" value="1" required/>
                                <div class="input-group-append">
                                    <span class="input-group-text bg-white border-0 small text-muted">packs</span>
                                </div>
                            </div>
                        </td>
                        <td class="text-right pr-3 py-3">
                            <button type="button" class="btn btn-outline-danger btn-sm border-0 rounded-circle delete-row" onclick="deleteRow(this, 0)" style="width: 35px; height: 35px;">
                                <i class="fas fa-trash"></i>
                            </button>
                        </td>
                    </tr>
                </template>

                <script>
                    function addRow(lineId) {
                        const tbody = document.getElementById('taskBody_' + lineId);
                        const template = document.getElementById('rowTemplate');
                        const clone = template.content.cloneNode(true);
                        clone.querySelector('.user-select').name = 'taskUser_' + lineId;
                        const packInput = clone.querySelector('.pack-input');
                        packInput.name = 'taskPacks_' + lineId;
                        packInput.setAttribute('oninput', 'validateLinePacks(' + lineId + ')');
                        clone.querySelector('.delete-row').setAttribute('onclick', 'deleteRow(this, ' + lineId + ')');
                        tbody.appendChild(clone);
                        updateDeleteButtons(lineId);
                        validateLinePacks(lineId);
                    }
                    
                    function deleteRow(btn, lineId) {
                        const row = btn.closest('tr');
                        row.remove();
                        if (lineId > 0) {
                            updateDeleteButtons(lineId);
                            validateLinePacks(lineId);
                        }
                    }
                    
                    function updateDeleteButtons(lineId) {
                        const rows = document.querySelectorAll('#taskBody_' + lineId + ' .task-row');
                        const deleteBtns = document.querySelectorAll('#taskBody_' + lineId + ' .delete-row');
                        deleteBtns.forEach(b => b.disabled = (rows.length === 1));
                    }
                    
                    function validateLinePacks(lineId) {
                        const card = document.getElementById('lineCard_' + lineId);
                        if (!card) return false;
                        const targetPacks = parseInt(card.getAttribute('data-target-packs'));
                        const packInputs = document.querySelectorAll('#taskBody_' + lineId + ' .pack-input');
                        let currentTotal = 0;
                        packInputs.forEach(input => currentTotal += parseInt(input.value || 0));
                        
                        const totalSpan = document.getElementById('currentTotal_' + lineId);
                        const validIcon = document.getElementById('validIcon_' + lineId);
                        totalSpan.textContent = currentTotal;
                        
                        if (currentTotal === targetPacks) {
                            totalSpan.className = 'text-success font-weight-bold';
                            card.classList.remove('border-danger');
                            validIcon.innerHTML = '<i class="fas fa-check-circle"></i>';
                            validIcon.className = 'ml-2 text-success';
                            return true;
                        } else {
                            totalSpan.className = 'text-danger font-weight-bold';
                            card.classList.add('border-danger');
                            validIcon.innerHTML = '<i class="fas fa-times-circle"></i>';
                            validIcon.className = 'ml-2 text-danger';
                            return false;
                        }
                    }
                    
                    function validateAllPacks() {
                        const cards = document.querySelectorAll('.line-card');
                        let allValid = true;
                        cards.forEach(card => {
                            if (!validateLinePacks(card.getAttribute('data-line-id'))) allValid = false;
                        });
                        if (!allValid) alert('Assignments incomplete. Assigned packs must match the required total for each line.');
                        return allValid;
                    }

                    document.addEventListener('DOMContentLoaded', () => {
                        document.querySelectorAll('.line-card').forEach(c => validateLinePacks(c.getAttribute('data-line-id')));
                    });
                </script>
            </c:when>
        </c:choose>
    </jsp:body>
</t:layout>

<style>
    .step-item { color: rgba(255,255,255,0.6); transition: all 0.3s; }
    .step-item.active { color: #fff; }
    .step-icon {
        width: 40px; height: 40px; border-radius: 50%;
        background: rgba(255,255,255,0.2); 
        display: flex; align-items: center; justify-content: center;
        font-weight: bold; border: 2px solid transparent;
    }
    .step-item.active .step-icon { background: #fff; color: #4e73df; }
    .step-connector { height: 2px; width: 60px; background: rgba(255,255,255,0.2); }
    .step-connector.active { background: #fff; }
    
    .icon-circle { width: 45px; height: 45px; display: flex; align-items: center; justify-content: center; border-radius: 50%; }
    .rounded-lg { border-radius: 12px!important; }
    .input-group-lg .form-control { height: calc(1.5em + 1.25rem + 2px); }
</style>
