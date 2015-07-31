package org.sharedhealth.healthId.web.service;


import org.sharedhealth.healthId.web.Model.GeneratedHidRange;
import org.sharedhealth.healthId.web.repository.GeneratedHidRangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneratedHidRangeService {

    private GeneratedHidRangeRepository generatedHidRangeRepository;

    @Autowired
    public GeneratedHidRangeService(GeneratedHidRangeRepository generatedHidRangeRepository) {
        this.generatedHidRangeRepository = generatedHidRangeRepository;
    }

    public List<GeneratedHidRange> getPreGeneratedHidRanges() {
        return generatedHidRangeRepository.getPreGeneratedHidRanges();
    }

    public GeneratedHidRange saveGeneratedHidRange(GeneratedHidRange generatedHidRange) {
        return generatedHidRangeRepository.saveGeneratedHidRange(generatedHidRange);
    }
}
