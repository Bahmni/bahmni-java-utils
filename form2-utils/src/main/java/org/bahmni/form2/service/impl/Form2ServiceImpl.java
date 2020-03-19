package org.bahmni.form2.service.impl;

import org.bahmni.form2.service.Form2Service;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.form2.utils.ResourceUtils.convertResourceOutputToString;

@Component
public class Form2ServiceImpl implements Form2Service {

    @Autowired
    private SessionFactory sessionFactory;
    @Value("classpath:sql/formList.sql")
    private Resource form2FormListResource;

    private Map<String, String> allLatestFormPaths;
    private Map<String, Integer> formNameToLatestVersionMap;


    public Map<String, String> getAllLatestFormPaths() {
        Map<String, String> formPaths = new HashMap<>();
        List<Object[]> formRows = executeFormListQuery();
        for (Object[] row : formRows) {
            String name = (String) row[1];
            String valueReference = (String) row[3];
            formPaths.put(name, valueReference);
        }
        return formPaths;
    }

    private List executeFormListQuery() {
        final String form2FormListQuery = convertResourceOutputToString(form2FormListResource);
        return sessionFactory.getCurrentSession().createSQLQuery(form2FormListQuery).list();
    }

    public Map<String, Integer> getFormNamesWithLatestVersionNumber() {
        LinkedHashMap<String, Integer> formNameAndVersionMap = new LinkedHashMap<>();
        List<Object[]> forms = getLatestFormNamesWithVersion();
        for (Object[] row : forms) {
            String name = (String) row[0];
            int version = Integer.parseInt((String) row[1]);
            formNameAndVersionMap.put(name, version);
        }
        return formNameAndVersionMap;
    }

    public int getFormLatestVersion(String formName) {
        if (formNameToLatestVersionMap == null) {
            formNameToLatestVersionMap = getFormNamesWithLatestVersionNumber();
        }
        return formNameToLatestVersionMap.get(formName);
    }

    @Override
    public String getFormPath(String formName) {
        if (allLatestFormPaths == null) {
            allLatestFormPaths = getAllLatestFormPaths();
        }
        return allLatestFormPaths.get(formName);
    }

    private List getLatestFormNamesWithVersion() {
        return sessionFactory.getCurrentSession()
                .createSQLQuery("SELECT name , MAX(version) as version FROM form GROUP BY name").list();
    }
}
