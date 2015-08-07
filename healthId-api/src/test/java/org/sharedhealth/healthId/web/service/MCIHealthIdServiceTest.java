package org.sharedhealth.healthId.web.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.healthId.web.Model.MciHealthId;
import org.sharedhealth.healthId.web.config.HealthIdProperties;
import org.sharedhealth.healthId.web.repository.HealthIdRepository;
import org.sharedhealth.healthId.web.utils.LuhnChecksumGenerator;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class MCIHealthIdServiceTest {

    HealthIdProperties properties;

    @Mock
    HealthIdRepository healthIdRepository;

    @Mock
    LuhnChecksumGenerator checksumGenerator;

    @Before
    public void setUp() throws Exception {
        properties = new HealthIdProperties();
        properties.setInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        properties.setMciStartHid("9800000000");
        properties.setMciEndHid("9999999999");
        properties.setHealthIdBlockSize("10");
        properties.setHealthIdBlockSizeThreshold("1");
        initMocks(this);

    }

    @Test
    public void validIdsStartWith9() {
        Pattern invalidPattern = Pattern.compile(properties.getInvalidHidPattern());
        assertTrue(invalidPattern.matcher("8801543886").find());
        assertFalse(invalidPattern.matcher("9801543886").find());
    }

    @Test
    public void validIdsStartWith98Or99() {
        Pattern invalidPattern = Pattern.compile(properties.getInvalidHidPattern());
        assertTrue(invalidPattern.matcher("9101543886").find());
        assertTrue(invalidPattern.matcher("98000034730").find());
        assertFalse(invalidPattern.matcher("9801543886").find());
        assertFalse(invalidPattern.matcher("9901543886").find());
    }

    @Test
    public void validIdsCannotHave4RepeatedChars() {
        Pattern invalidPattern = Pattern.compile(properties.getInvalidHidPattern());
        assertTrue(invalidPattern.matcher("98015888861").find());
        assertFalse(invalidPattern.matcher("9901548886").find());
    }

    @Test
    public void validIdsCannotHave2OrMoreInstancesOf3RepeatedChars() {
        Pattern invalidPattern = Pattern.compile(properties.getInvalidHidPattern());
        assertFalse(invalidPattern.matcher("9801588861").find());
        assertTrue(invalidPattern.matcher("9991548886").find());
        assertTrue(invalidPattern.matcher("9991118126").find());
        assertTrue(invalidPattern.matcher("9811115255").find());
    }

    @Test
    public void validIdsCannotHaveMoreThan10Chars() {
        Pattern invalidPattern = Pattern.compile(properties.getInvalidHidPattern());
        assertFalse(invalidPattern.matcher("9801588861").find());
        assertTrue(invalidPattern.matcher("999154128886").find());
        assertTrue(invalidPattern.matcher("9926").find());
    }

    @Test
    public void shouldExecuteConfiguredInvalidRegex() {
        HealthIdProperties testProperties = new HealthIdProperties();
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[45]\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator);
        assertEquals(78, healthIdService.generate(0, 99));
    }

    @Test
    public void shouldExecuteCorrectMCIHIDRegex() {
        long start = 9800005790L, end = 9800005792L;
        HealthIdProperties testProperties = new HealthIdProperties();
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[^9]|^.[^89]|(^\\d{0,9}$)|(^\\d{11,}$)|((\\d)\\4{2})\\d*((\\d)\\6{2})|(\\d)\\7{3}");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator);
        assertEquals(0, healthIdService.generate(9800005790L, 9800005792L));
    }

    @Test
    public void shouldSaveValidHids() {
        when(healthIdRepository.saveHealthId(any(MciHealthId.class))).thenReturn(MciHealthId.NULL_HID);
        when(checksumGenerator.generate(any(String.class))).thenReturn(1);

        HealthIdProperties testProperties = new HealthIdProperties();
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[45]\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator);
        assertEquals(78, healthIdService.generate(0, 99));

        ArgumentCaptor<MciHealthId> healthIdArgumentCaptor = ArgumentCaptor.forClass(MciHealthId.class);
        verify(healthIdRepository, times(78)).saveHealthId(healthIdArgumentCaptor.capture());
        verify(checksumGenerator, times(78)).generate(any(String.class));
        assertTrue(String.valueOf(healthIdArgumentCaptor.getValue().getHid()).endsWith("1"));
    }

    @Test
    public void shouldNotGenerateAnyHidsIfStartHidIsGreaterThanEndHid() {
        HealthIdProperties testProperties = new HealthIdProperties();
        // This regex will match any number starting with 4 or 5
        // and we will mark it as invalid
        testProperties.setInvalidHidPattern("^[45]\\d*$");
        HealthIdService healthIdService = new HealthIdService(testProperties, healthIdRepository, checksumGenerator);
        assertEquals(0, healthIdService.generate(100, 99));
    }

    @Test
    public void should10kBlockIdsForMCIService() {
        ArrayList<MciHealthId> result = new ArrayList<>();
        result.add(new MciHealthId("898998"));
        result.add(new MciHealthId("898999"));
        when(healthIdRepository.getNextBlock(properties.getHealthIdBlockSize())).thenReturn(result);

        HealthIdService healthIdService = new HealthIdService(properties, healthIdRepository, checksumGenerator);
        List<MciHealthId> nextBlock = healthIdService.getNextBlock();
        verify(healthIdRepository).getNextBlock(properties.getHealthIdBlockSize());
        assertEquals(2, nextBlock.size());
    }

    @Test
    public void shouldMarkHidUsed() {
        MciHealthId MciHealthId = new MciHealthId("898998");
        doNothing().when(healthIdRepository).removedUsedHid(any(MciHealthId.class));
        HealthIdService healthIdService = new HealthIdService(properties, healthIdRepository, checksumGenerator);
        healthIdService.markUsed(MciHealthId);
        verify(healthIdRepository).removedUsedHid(MciHealthId);
    }

}