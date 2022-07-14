package org.bahmni.form2;

import org.bahmni.form2.service.Form2Service;
import org.bahmni.form2.service.impl.Form2ServiceImpl;
import org.bahmni.form2.utils.ResourceUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.bahmni.form2.service.impl.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.form2.utils.ResourceUtils.convertResourceOutputToString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ResourceUtils.class)
public class Form2ServiceImplTest {
    Form2Service form2Service;
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Resource form2FormListResource;
    private String sql;

    @Before
    public void setUp() throws Exception {
        mockStatic(ResourceUtils.class);
        sql = "form list sql";
        when(convertResourceOutputToString(any(Resource.class))).thenReturn(sql);
        form2Service = new Form2ServiceImpl();
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        setValuesForMemberFields(form2Service, "sessionFactory", sessionFactory);
        setValuesForMemberFields(form2Service, "form2FormListResource", form2FormListResource);
    }

    @Test
    public void shouldReturnMapWithKeysAsFormNamesAndValuesAsLatestVersion() {

        String formNameAndVersionSql = "SELECT name , MAX(version) as version FROM form GROUP BY name";
        List<Object[]> formRow = new ArrayList<>();
        formRow.add(Arrays.asList("Vitals", "3").toArray());
        final NativeQuery sqlQuery = mock(NativeQuery.class);
        when(session.createSQLQuery(formNameAndVersionSql)).thenReturn(sqlQuery);
        when(sqlQuery.list()).thenReturn(formRow);

        Map<String, Integer> formNameAndVersionMap = form2Service.getFormNamesWithLatestVersionNumber();

        assertEquals(3, formNameAndVersionMap.get("Vitals").intValue());
    }

    @Test
    public void shouldReturnEmptyMapWhenNoFormsAvailable() {

        String formNameAndVersionSql = "SELECT name , MAX(version) as version FROM form GROUP BY name";

        final NativeQuery sqlQuery = mock(NativeQuery.class);
        when(session.createSQLQuery(formNameAndVersionSql)).thenReturn(sqlQuery);
        when(sqlQuery.list()).thenReturn(new ArrayList<>());

        Map<String, Integer> formNameAndVersionMap = form2Service.getFormNamesWithLatestVersionNumber();

        assertEquals(0, formNameAndVersionMap.size());
    }

    @Test
    public void shouldReturnAllFormsAndTheirLocations() {

        addTestMocksBehavior();

        final Map<String, String> allLatestFormPaths = form2Service.getAllLatestFormPaths();

        assertEquals(1, allLatestFormPaths.size());
        assertEquals("/home/bahmni/clinical_forms/Vitals_1.json", allLatestFormPaths.get("Vitals"));
        verify(ResourceUtils.class);
        convertResourceOutputToString(form2FormListResource);
    }

    private void addTestMocksBehavior() {
        List<Object[]> formRow = new ArrayList<>();
        formRow.add(Arrays.asList("", "Vitals", "", "/home/bahmni/clinical_forms/Vitals_1.json").toArray());
        final NativeQuery sqlQuery = mock(NativeQuery.class);
        when(session.createSQLQuery(anyString())).thenReturn(sqlQuery);
        when(sqlQuery.list()).thenReturn(formRow);
    }

    @Test
    public void shouldReturnFormJsonPathFromGivenFormName() {

        addTestMocksBehavior();

        final String formPath = form2Service.getFormPath("Vitals");
        assertEquals("/home/bahmni/clinical_forms/Vitals_1.json", formPath);
        verify(ResourceUtils.class);
        convertResourceOutputToString(form2FormListResource);
    }

    @Test
    public void verifyGetAllLatestFormPathsIsCalledOnlyOnce() {

        addTestMocksBehavior();

        // multiple calls
        final String formPath = form2Service.getFormPath("Vitals");
        form2Service.getFormPath("History Examination");

        assertEquals("/home/bahmni/clinical_forms/Vitals_1.json", formPath);
        verify(ResourceUtils.class);
        convertResourceOutputToString(form2FormListResource);
    }

    @Test
    public void shouldGetLatestVersionOfAGivenForm() {
        String formNameAndVersionSql = "SELECT name , MAX(version) as version FROM form GROUP BY name";
        List<Object[]> formRow = new ArrayList<>();
        formRow.add(Arrays.asList("Vitals", "3").toArray());
        final NativeQuery sqlQuery = mock(NativeQuery.class);
        when(session.createSQLQuery(formNameAndVersionSql)).thenReturn(sqlQuery);
        when(sqlQuery.list()).thenReturn(formRow);

        assertEquals(3, form2Service.getFormLatestVersion("Vitals"));

    }
}
