-- /
-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
-- the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
-- 
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.
-- /

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
