SELECT
  form.form_id,
  form.name,
  form.version,
  fr.value_reference
FROM form
  INNER JOIN form_resource fr ON form.form_id = fr.form_id
  INNER JOIN (select
                name,
                MAX(version) as version
              from form
              where published = true
              group by name) as MaxForm
  on form.name = MaxForm.name and form.version = MaxForm.version
  WHERE form.retired = FALSE AND form.published = TRUE AND fr.datatype = 'org.bahmni.customdatatype.datatype.FileSystemStorageDatatype';
