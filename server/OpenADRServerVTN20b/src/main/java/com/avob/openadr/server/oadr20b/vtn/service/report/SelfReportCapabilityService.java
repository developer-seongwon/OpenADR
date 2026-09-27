package com.avob.openadr.server.oadr20b.vtn.service.report;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.SelfReportCapability;
import com.avob.openadr.server.oadr20b.vtn.models.venreport.capability.SelfReportCapabilityDao;
import com.avob.openadr.server.oadr20b.vtn.service.GenericService;

@Service
public class SelfReportCapabilityService extends GenericService<SelfReportCapability> {

    @Resource
    private SelfReportCapabilityDao selfReportCapabilityDao;
    public SelfReportCapability findByReportSpecifierId(String reportSpecifierId) {
    	 List<SelfReportCapability> findByReportSpecifierId = selfReportCapabilityDao.findByReportSpecifierId(reportSpecifierId);
    	 if(findByReportSpecifierId.isEmpty()) {
    		 return null;
    	 }
        return findByReportSpecifierId.get(0);
    }

    @Override
    public JpaRepository<SelfReportCapability, Long> getDao() {
        return selfReportCapabilityDao;
    }

}
