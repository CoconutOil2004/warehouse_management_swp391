<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
        <%@taglib uri="jakarta.tags.core" prefix="c" %>
            <t:layout title="Update Shipment: ${shipment.shipmentNumber}">
                <div class="row justify-content-center">
                    <div class="col-lg-7">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3 bg-gradient-warning text-white">
                                <h6 class="m-0 font-weight-bold"><i class="fas fa-sync-alt mr-2"></i>Update Shipment
                                    Status</h6>
                            </div>
                            <div class="card-body">
                                <form action="${pageContext.request.contextPath}/shipment" method="post"
                                    id="updateStatusForm">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="id" value="${shipment.shipmentId}">

                                    <div class="mb-4">
                                        <label class="form-label font-weight-bold">Current Status</label>
                                        <div class="p-3 bg-light rounded mb-2 border-left-warning h5 font-weight-bold">
                                            <c:choose>
                                                <c:when test="${shipment.status == 'CREATED'}">Created (CREATED)
                                                </c:when>
                                                <c:when test="${shipment.status == 'PICKED_UP'}">Picked Up (PICKED_UP)
                                                </c:when>
                                                <c:when test="${shipment.status == 'IN_TRANSIT'}">In Transit
                                                    (IN_TRANSIT)</c:when>
                                                <c:when test="${shipment.status == 'DELIVERED'}">Delivered
                                                    (DELIVERED)</c:when>
                                                <c:when test="${shipment.status == 'CANCELLED'}">Cancelled/Failed
                                                    (CANCELLED)</c:when>
                                                <c:otherwise>${shipment.status}</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <div class="row mb-4">
                                        <div class="col-md-12">
                                            <label class="form-label font-weight-bold">Change status to</label>
                                            <select class="form-control form-control-lg border-primary" name="status"
                                                id="statusSelect" required>
                                                <option value="CREATED" ${shipment.status=='CREATED' ? 'selected' : ''
                                                    }>Created (CREATED)</option>
                                                <option value="PICKED_UP" ${shipment.status=='PICKED_UP' ? 'selected'
                                                    : '' }>Picked Up (PICKED_UP)</option>
                                                <option value="IN_TRANSIT" ${shipment.status=='IN_TRANSIT' ? 'selected'
                                                    : '' }>In Transit (IN_TRANSIT)</option>
                                                <option value="DELIVERED" ${shipment.status=='DELIVERED' ? 'selected'
                                                    : '' }>Delivered (DELIVERED)</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="row mb-4">
                                        <div class="col-md-12">
                                            <label class="form-label font-weight-bold">Update Carrier</label>
                                            <select class="form-control" name="carrierId" ${shipment.status !='CREATED'
                                                ? 'disabled' : '' }>
                                                <c:forEach var="c" items="${carriers}">
                                                    <option value="${c.carrierId}" ${shipment.carrierId==c.carrierId
                                                        ? 'selected' : '' }>${c.name}</option>
                                                </c:forEach>
                                            </select>
                                            <c:if test="${shipment.status != 'CREATED'}">
                                                <input type="hidden" name="carrierId" value="${shipment.carrierId}">
                                            </c:if>
                                        </div>
                                    </div>

                                    <div class="mb-4">
                                        <label class="form-label font-weight-bold">Tracking Code  <span
                                                class="text-danger">*</span></label>
                                        <input type="text" class="form-control" name="trackingCode"
                                            value="${shipment.trackingCode}" placeholder="Update tracking code..."
                                            ${(shipment.status == 'PICKED_UP' || shipment.status == 'IN_TRANSIT' || shipment.status == 'DELIVERED' || shipment.status == 'CANCELLED') ? 'readonly' : ''}
                                            required>
                                    </div>

                                    <div class="mb-4">
                                        <label class="form-label font-weight-bold">Action Notes</label>
                                        <textarea class="form-control" name="note" rows="3"
                                            placeholder="Add status change reason or delivery notes...">${shipment.note}</textarea>
                                    </div>

                                    <div class="alert alert-info small">
                                        <i class="fas fa-info-circle mr-1"></i> Changing status to <b>IN TRANSIT</b> or
                                        <b>DELIVERED</b> will automatically record the current timestamp.
                                    </div>

                                    <hr>
                                    <div class="d-flex justify-content-between">
                                        <a href="${pageContext.request.contextPath}/shipment?action=detail&id=${shipment.shipmentId}"
                                            class="btn btn-light px-4">Cancel</a>
                                        <button type="button" onclick="confirmUpdate()"
                                            class="btn btn-warning px-5 font-weight-bold shadow-sm">
                                            <i class="fas fa-save mr-1"></i> Save Changes
                                        </button>
                                    </div>
                                </form>
                                <script>
                                    document.addEventListener('DOMContentLoaded', function() {
                                        const carrierSelect = document.querySelector('select[name="carrierId"]');
                                        const trackingInput = document.querySelector('input[name="trackingCode"]');
                                        // Status will dictate if inputs are completely locked from the server-side logic above.
                                        const isLockedByStatus = trackingInput.hasAttribute('readonly') && "${shipment.status}" !== "CREATED";
                                        
                                        function generateInternalCode() {
                                            const date = new Date();
                                            const ms = date.getMilliseconds();
                                            const sec = date.getSeconds();
                                            const min = date.getMinutes();
                                            const hour = date.getHours();
                                            return "INT-" + date.getFullYear() + (date.getMonth()+1) + date.getDate() + "-" + hour + min + sec + ms;
                                        }

                                        function updateTrackingFieldState() {
                                            if (isLockedByStatus) return; // Keep it locked if shipped/picked up
                                            
                                            // Handle case where select is disabled (read-only state) but we need to check its value
                                            let selectedOption = null;
                                            if (carrierSelect && !carrierSelect.disabled && carrierSelect.selectedIndex >= 0) {
                                                selectedOption = carrierSelect.options[carrierSelect.selectedIndex];
                                            } else {
                                                // Try to look up by the hidden input value if select is disabled
                                                const hiddenCarrierId = document.querySelector('input[type="hidden"][name="carrierId"]');
                                                if (hiddenCarrierId && hiddenCarrierId.value) {
                                                    const option = document.querySelector(`select[name="carrierId"] option[value="${hiddenCarrierId.value}"]`);
                                                    if(option) selectedOption = option;
                                                }
                                            }

                                            if (selectedOption) {
                                                const text = selectedOption.text;
                                                if (text.includes('(INTERNAL)') || text.includes('Giao hàng nội bộ')) {
                                                    if (!trackingInput.value || trackingInput.value.trim() === '') {
                                                        trackingInput.value = generateInternalCode();
                                                    }
                                                    trackingInput.readOnly = true;
                                                    trackingInput.classList.add('bg-light');
                                                } else {
                                                    trackingInput.readOnly = false;
                                                    trackingInput.classList.remove('bg-light');
                                                }
                                            }
                                        }
                                        
                                        if(carrierSelect) {
                                            carrierSelect.addEventListener('change', updateTrackingFieldState);
                                        }
                                        
                                        // Run once on load to establish correct state
                                        updateTrackingFieldState();
                                    });

                                    function confirmUpdate() {
                                        const statusText = document.getElementById('statusSelect').options[document.getElementById('statusSelect').selectedIndex].text;
                                        if (confirm("Are you sure you want to update the shipment status to: " + statusText + "?")) {
                                            // Ensure a carrier tracking code exists
                                            const trackingCode = document.querySelector('input[name="trackingCode"]').value;
                                            if (!trackingCode || trackingCode.trim() === '') {
                                                 alert("Please enter a tracking code.");
                                                 return;
                                            }
                                            document.getElementById('updateStatusForm').submit();
                                        }
                                    }
                                </script>
                            </div>
                        </div>
                    </div>
                </div>
            </t:layout>