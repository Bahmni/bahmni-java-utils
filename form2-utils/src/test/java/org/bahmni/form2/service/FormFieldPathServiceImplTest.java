package org.bahmni.form2.service;

import org.bahmni.form2.exception.InvalidFormException;
import org.bahmni.form2.service.impl.Form2ReaderServiceImpl;
import org.bahmni.form2.service.impl.FormFieldPathServiceImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FormFieldPathServiceImplTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private Form2Service form2Service;
    private FormFieldPathService formFieldPathService;

    @Before
    public void setUp() {
        initMocks(this);
        formFieldPathService = new FormFieldPathServiceImpl(form2Service, new Form2ReaderServiceImpl());
    }

    @Test
    public void shouldReturnFormFieldOfHeightObsControl() {

        when(form2Service.getFormPath("Vitals")).thenReturn("src/test/resources/Vitals_1.json");
        when(form2Service.getFormLatestVersion("Vitals")).thenReturn(1);
        final String formFieldPath = formFieldPathService.getFormFieldPath(asList("Vitals", "Height"));

        assertEquals("Vitals.1/1-0", formFieldPath);
    }

    @Test
    public void shouldThrowInvalidFormExceptionIfInvalidFormNameIsGiven() {

        when(form2Service.getFormPath("Vitals")).thenReturn(null);
        exception.expect(InvalidFormException.class);
        exception.expectMessage("Vitals not found");

        formFieldPathService.getFormFieldPath(asList("Vitals", "Height"));
    }

    @Test
    public void shouldVerifyFormFieldPathsInTheGivenForm() {

        when(form2Service.getFormPath("ComplexForm")).thenReturn("src/test/resources/ComplexForm_1.json");
        when(form2Service.getFormLatestVersion("ComplexForm")).thenReturn(1);

        final String sectionFormFieldPath = formFieldPathService.getFormFieldPath(asList("ComplexForm", "Section"));
        assertEquals("ComplexForm.1/1-0", sectionFormFieldPath);

        final String dateFormFieldPath = formFieldPathService.getFormFieldPath(asList("ComplexForm", "Section", "Date"));
        assertEquals("ComplexForm.1/8-0", dateFormFieldPath);

        final String bmiDataFormFieldPath = formFieldPathService.getFormFieldPath(asList("ComplexForm", "BMI Data"));
        assertEquals("ComplexForm.1/4-0", bmiDataFormFieldPath);

        final String bmiFormFieldPath = formFieldPathService.getFormFieldPath(asList("ComplexForm", "BMI Data", "BMI"));
        assertEquals("ComplexForm.1/5-0", bmiFormFieldPath);

        final String bmiAbnormalFormFieldPath = formFieldPathService.getFormFieldPath(asList("ComplexForm", "BMI Data", "BMI ABNORMAL"));
        assertEquals("ComplexForm.1/6-0", bmiAbnormalFormFieldPath);
    }

    @Test
    public void shouldReturnFormFieldPathForAnObsInsideSectionWithAddMoreProperty() {

        when(form2Service.getFormPath("AddMore")).thenReturn("src/test/resources/AddMore.json");
        when(form2Service.getFormLatestVersion("AddMore")).thenReturn(1);

        final String sectionFormFieldPath = formFieldPathService.getFormFieldPath(asList("AddMore", "BMI Add more"));
        assertEquals("AddMore.1/1-0", sectionFormFieldPath);

        final String heightFormFieldPath = formFieldPathService.getFormFieldPath(asList("AddMore", "BMI Add more",
                "Height (cm)"));
        assertEquals("AddMore.1/1-0/2-0", heightFormFieldPath);
    }

    @Test
    public void shouldReturnFormFieldPathForAnObsWhoseParentSectionIsAddMoreAndGrandParentSectionIsNotAddMore() {

        when(form2Service.getFormPath("AddMoreInsideASection"))
                .thenReturn("src/test/resources/AddMoreInsideASection.json");
        when(form2Service.getFormLatestVersion("AddMoreInsideASection")).thenReturn(1);

        final String sectionFormFieldPath = formFieldPathService.getFormFieldPath(asList("AddMoreInsideASection",
                "First Section"));
        assertEquals("AddMoreInsideASection.1/2-0", sectionFormFieldPath);

        final String innerSectionFormFieldPath = formFieldPathService.getFormFieldPath(asList("AddMoreInsideASection",
                "First Section", "Inside Section"));
        assertEquals("AddMoreInsideASection.1/4-0", innerSectionFormFieldPath);

        final String innerObsControlFormFieldPath = formFieldPathService.getFormFieldPath(asList("AddMoreInsideASection",
                "First Section", "Inside Section", "Height (cm)"));
        assertEquals("AddMoreInsideASection.1/4-0/5-0", innerObsControlFormFieldPath);
    }

    @Test
    public void shouldReturnFormFieldPathForAnObsWhoseParentSectionIsAddMoreAndGrandParentSectionIsAlsoAddMore() {

        when(form2Service.getFormPath("AddMoreInsideAddMore"))
                .thenReturn("src/test/resources/AddMoreInsideAddMore.json");
        when(form2Service.getFormLatestVersion("AddMoreInsideAddMore")).thenReturn(1);

        final String sectionFormFieldPath = formFieldPathService.getFormFieldPath(asList("AddMoreInsideAddMore",
                "First Section"));
        assertEquals("AddMoreInsideAddMore.1/2-0", sectionFormFieldPath);

        final String innerSectionFormFieldPath = formFieldPathService.getFormFieldPath(asList("AddMoreInsideAddMore",
                "First Section", "Inside Section"));
        assertEquals("AddMoreInsideAddMore.1/2-0/4-0", innerSectionFormFieldPath);

        final String innerObsControlFormFieldPath = formFieldPathService.getFormFieldPath(asList("AddMoreInsideAddMore",
                "First Section", "Inside Section", "Height (cm)"));
        assertEquals("AddMoreInsideAddMore.1/2-0/4-0/5-0", innerObsControlFormFieldPath);
    }

    @Test
    public void shouldReturnFormFieldPathForAnObsInsideMultipleSectionsWithAddMoreProperties() {

        when(form2Service.getFormPath("AddMoreInsideMultipleSections"))
                .thenReturn("src/test/resources/AddMoreInsideMultipleSections.json");
        when(form2Service.getFormLatestVersion("AddMoreInsideMultipleSections")).thenReturn(1);

        final String firstSectionFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("AddMoreInsideMultipleSections", "First Section"));
        assertEquals("AddMoreInsideMultipleSections.1/1-0", firstSectionFormFieldPath);

        final String weightObsFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("AddMoreInsideMultipleSections", "First Section", "Weight (kg)"));
        assertEquals("AddMoreInsideMultipleSections.1/2-0", weightObsFormFieldPath);

        final String secondSectionFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("AddMoreInsideMultipleSections", "First Section", "Second Section Add More"));
        assertEquals("AddMoreInsideMultipleSections.1/3-0", secondSectionFormFieldPath);

        final String cD4ObsFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("AddMoreInsideMultipleSections", "First Section", "Second Section Add More", "CD4 date"));
        assertEquals("AddMoreInsideMultipleSections.1/3-0/4-0", cD4ObsFormFieldPath);

        final String thirdSectionFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("AddMoreInsideMultipleSections", "First Section", "Second Section Add More", "Third Section"));
        assertEquals("AddMoreInsideMultipleSections.1/3-0/5-0", thirdSectionFormFieldPath);

        final String visitDateObsFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("AddMoreInsideMultipleSections", "First Section", "Second Section Add More", "Third Section",
                        "Followup, Visit Date"));
        assertEquals("AddMoreInsideMultipleSections.1/3-0/5-0/7-0", visitDateObsFormFieldPath);

        final String innerSectionFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("AddMoreInsideMultipleSections", "First Section", "Second Section Add More", "Third Section",
                        "Inner Section Add more"));
        assertEquals("AddMoreInsideMultipleSections.1/3-0/5-0/8-0", innerSectionFormFieldPath);

        final String heightObsFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("AddMoreInsideMultipleSections", "First Section", "Second Section Add More", "Third Section",
                        "Inner Section Add more", "Height (cm)"));
        assertEquals("AddMoreInsideMultipleSections.1/3-0/5-0/8-0/9-0", heightObsFormFieldPath);

    }

    @Test
    public void shouldReturnFormFieldPathForObsInsideParallelSections() {

        when(form2Service.getFormPath("SameLevelSection"))
                .thenReturn("src/test/resources/SameLevelSection.json");
        when(form2Service.getFormLatestVersion("SameLevelSection")).thenReturn(1);

        final String firstSectionFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("SameLevelSection", "First Section"));
        assertEquals("SameLevelSection.1/3-0", firstSectionFormFieldPath);

        final String weightObsFormFieldPath = formFieldPathService.getFormFieldPath(asList("SameLevelSection",
                "First Section", "Weight (kg)"));
        assertEquals("SameLevelSection.1/5-0", weightObsFormFieldPath);

        final String secondSectionFormFieldPath = formFieldPathService.getFormFieldPath(
                asList("SameLevelSection", "Second Section With Add More"));
        assertEquals("SameLevelSection.1/4-0", secondSectionFormFieldPath);

        final String heightObsFormFieldPath = formFieldPathService.getFormFieldPath(asList("SameLevelSection",
                "Second Section With Add More", "Height (cm)"));
        assertEquals("SameLevelSection.1/4-0/6-0", heightObsFormFieldPath);
    }

    @Test
    public void shouldReturnMultiSelectAttributeOfObsControl() {

        when(form2Service.getFormPath("Form2EncountersTest")).thenReturn("src/test/resources/Form2Encounters.json");
        when(form2Service.getFormLatestVersion("Form2EncountersTest")).thenReturn(1);
        final boolean isMultiSelectObs = formFieldPathService.isMultiSelectObs(asList("Form2EncountersTest", "HIV Infection History", "WHO Stage Conditions"));

        assertTrue(isMultiSelectObs);
    }

    @Test
    public void shouldReturnMandatoryAttributeOfObsControl() {

        when(form2Service.getFormPath("Form2EncountersTest")).thenReturn("src/test/resources/Form2Encounters.json");
        when(form2Service.getFormLatestVersion("Form2EncountersTest")).thenReturn(1);
        final boolean isMandatoryObs = formFieldPathService.isMandatory(asList("Form2EncountersTest", "HIV Infection History", "WHO Stage Conditions"));

        assertTrue(isMandatoryObs);
    }

    @Test
    public void shouldReturnAddmoreAttributeOfObsControl() {

        when(form2Service.getFormPath("Form2EncountersTest")).thenReturn("src/test/resources/Form2Encounters.json");
        when(form2Service.getFormLatestVersion("Form2EncountersTest")).thenReturn(1);
        final boolean isAddmore = formFieldPathService.isAddmore(asList("Form2EncountersTest", "HIV Infection History", "Source case ID"));

        assertTrue(isAddmore);
    }

    @Test
    public void shouldReturnAllowFutureDateAttributeOfObsControl() {

        when(form2Service.getFormPath("Form2EncountersTest")).thenReturn("src/test/resources/Form2Encounters.json");
        when(form2Service.getFormLatestVersion("Form2EncountersTest")).thenReturn(1);
        final boolean isAllowFutureDates = formFieldPathService.isAllowFutureDates(asList("Form2EncountersTest", "HIV Infection History", "Followup, Visit Date"));

        assertTrue(isAllowFutureDates);
    }

    @Test
    public void shouldReturnTrueForValidCSVHeader() {

        when(form2Service.getFormPath("Form2EncountersTest")).thenReturn("src/test/resources/Form2Encounters.json");
        when(form2Service.getFormLatestVersion("Form2EncountersTest")).thenReturn(1);
        final boolean isValidCSVHeader = formFieldPathService.isValidCSVHeader(asList("Form2EncountersTest", "HIV Infection History", "Followup, Visit Date"));

        assertTrue(isValidCSVHeader);
    }

    @Test
    public void shouldReturnFalseForInvalidCSVHeader() {

        when(form2Service.getFormPath("Form2EncountersTest")).thenReturn("src/test/resources/Form2Encounters.json");
        when(form2Service.getFormLatestVersion("Form2EncountersTest")).thenReturn(1);
        final boolean isValidCSVHeader = formFieldPathService.isValidCSVHeader(asList("Form2EncountersTest", "HIV Infection History Invalid", "Followup, Visit Date"));

        assertFalse(isValidCSVHeader);
    }
}
