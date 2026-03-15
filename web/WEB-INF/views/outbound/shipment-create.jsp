<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
        <%@taglib uri="jakarta.tags.core" prefix="c" %>
            <t:layout title="Create New Shipment">
                <div class="row justify-content-center">
                    <div class="col-lg-8">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                                <h6 class="m-0 font-weight-bold text-primary">Shipment Details</h6>
                                <a href="${pageContext.request.contextPath}/shipment?action=list"
                                    class="btn btn-sm btn-secondary rounded-pill">
                                    <i class="fas fa-arrow-left mr-1"></i> Back to List
                                </a>
                            </div>
                            <div class="card-body">
                                <form action="${pageContext.request.contextPath}/shipment" method="post"
                                    id="createShipmentForm">
                                    <input type="hidden" name="action" value="store">

                                    <div class="row mb-4">
                                        <div class="col-md-6">
                                            <label class="form-label font-weight-bold">Shipment Code <span
                                                    class="text-danger">*</span></label>
                                            <input type="text" class="form-control bg-light" name="shipmentNumber"
                                                value="${nextShipmentNumber}" readonly>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label font-weight-bold">Shipment Type</label>
                                            <select class="form-control" name="shipmentType">
                                                <option value="CUSTOMER">Customer Delivery (CUSTOMER)</option>
                                                <option value="TRANSFER">Internal Transfer (TRANSFER)</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="row mb-4">
                                        <div class="col-md-12">
                                            <label class="form-label font-weight-bold">Select Goods Delivery Note (GDN)
                                                <span class="text-danger">*</span></label>
                                            <select class="form-control select2" name="gdnId" required>
                                                <option value="">-- Choose a Confirmed GDN --</option>
                                                <c:forEach var="g" items="${gdns}">
                                                    <option value="${g.gdnId}" ${selectedGdnId != null && selectedGdnId == g.gdnId ? 'selected' : ''}>${g.gdnNumber}</option>
                                                </c:forEach>
                                            </select>
                                            <small class="text-muted">Only confirmed GDNs that haven't been shipped are
                                                listed.</small>
                                        </div>
                                    </div>

                                    <div class="row mb-4">
                                        <div class="col-md-6">
                                            <label class="form-label font-weight-bold">Carrier <span
                                                    class="text-danger">*</span></label>
                                            <select class="form-control" name="carrierId" required>
                                                <option value="">-- Select Courier --</option>
                                                <c:forEach var="c" items="${carriers}">
                                                    <option value="${c.carrierId}">${c.name} (${c.carrierType})</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label font-weight-bold">Tracking Code  <span
                                                    class="text-danger">*</span></label>
                                            <input type="text" class="form-control" name="trackingCode"
                                                placeholder="e.g. VN123456789..." required>
                                        </div>
                                    </div>

                                    <div class="mb-4">
                                        <label class="form-label font-weight-bold">Notes</label>
                                        <textarea class="form-control" name="note" rows="3"
                                            placeholder="Additional shipping instructions..."></textarea>
                                    </div>

                                    <hr>
                                    <div class="d-flex justify-content-end gap-2">
                                        <button type="reset" class="btn btn-light rounded-pill px-4">Clear Form</button>
                                        <button type="submit" class="btn btn-primary rounded-pill px-5 shadow-sm">
                                            <i class="fas fa-check-circle mr-1"></i> Register Shipment
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                <script>
                    document.addEventListener('DOMContentLoaded', function() {
                        const carrierSelect = document.querySelector('select[name="carrierId"]');
                        const trackingInput = document.querySelector('input[name="trackingCode"]');
                        
                        function generateInternalCode() {
                            const date = new Date();
                            const ms = date.getMilliseconds();
                            const sec = date.getSeconds();
                            const min = date.getMinutes();
                            const hour = date.getHours();
                            return "INT-" + date.getFullYear() + (date.getMonth()+1) + date.getDate() + "-" + hour + min + sec + ms;
                        }

                        carrierSelect.addEventListener('change', function() {
                            const selectedOption = this.options[this.selectedIndex];
                            const text = selectedOption.text;
                            
                            if (text.includes('(INTERNAL)') || text.includes('Giao hàng nội bộ')) {
                                trackingInput.value = generateInternalCode();
                                trackingInput.readOnly = true;
                                trackingInput.classList.add('bg-light');
                            } else {
                                trackingInput.value = '';
                                trackingInput.readOnly = false;
                                trackingInput.classList.remove('bg-light');
                            }
                        });
                    });
                </script>
            </t:layout>