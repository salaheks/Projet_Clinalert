package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final MeasurementRepository measurementRepository;

    private static final double STD_DEV_THRESHOLD = 2.5;

    public boolean isAnomaly(Measurement newMeasurement) {
        List<Measurement> history = measurementRepository.findTop20ByPatientIdAndTypeOrderByTimestampDesc(
                newMeasurement.getPatientId(), newMeasurement.getType());

        if (history.size() < 5) {
            // Not enough data to establish a trend
            return false;
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Measurement m : history) {
            stats.addValue(m.getValue());
        }

        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();

        // Avoid division by zero or extremely sensitive triggers on flat lines
        if (stdDev < 0.1) {
            return Math.abs(newMeasurement.getValue() - mean) > (mean * 0.2); // 20% deviation if stable
        }

        double zScore = Math.abs((newMeasurement.getValue() - mean) / stdDev);
        return zScore > STD_DEV_THRESHOLD;
    }
}
