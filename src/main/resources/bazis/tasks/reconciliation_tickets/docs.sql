DECLARE @person TABLE (
  centralId INT,
  localId INT,
  docSeries VARCHAR(10),
  docNumber VARCHAR(10)
)

SET NOCOUNT ON

--insert

SELECT 
  person = person.centralId,  
  docType = docType.GUID
FROM @person person
  LEFT JOIN WM_ACTDOCUMENTS doc 
    JOIN PPR_DOC docType ON docType.A_ID = doc.DOCUMENTSTYPE
  ON doc.PERSONOUID = person.localId
    AND ISNULL(doc.DOCUMENTSERIES, '') = ISNULL(person.docSeries, '')
    AND ISNULL(doc.DOCUMENTSNUMBER, '') = ISNULL(person.docNumber, '')
    AND ISNULL(doc.A_STATUS, 10) = 10