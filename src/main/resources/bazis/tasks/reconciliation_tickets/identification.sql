DECLARE @citizen TABLE (
  listIndex INT,
  surname VARCHAR(40),
  name VARCHAR(40),
  patronymic VARCHAR(40),
  birthdate DATE
)

SET NOCOUNT ON
SET DATEFORMAT ymd

--insert

SELECT 
  [listIndex] = imported.listIndex,
  [centralId] = person.A_OUID,  
  [borough] = person.A_SERV,
  [localId] = person.A_LOCAL_OUID,  
  [snils] = person.A_SNILS,
  [passport.series] = passport.DOCUMENTSERIES,
  [passport.number] = passport.DOCUMENTSNUMBER
FROM @citizen imported
  JOIN REGISTER_PERSONAL_CARD person 
    JOIN SPR_FIO_SURNAME surname ON surname.OUID = person.SURNAME
    JOIN SPR_FIO_NAME name ON name.OUID = person.A_NAME
    JOIN SPR_FIO_SECONDNAME patronymic ON patronymic.OUID = person.A_SECONDNAME
  ON surname.A_NAME = imported.surname
    AND name.A_NAME = imported.name
    AND patronymic.A_NAME = imported.patronymic
    AND CAST(person.BIRTHDATE AS DATE) = imported.birthdate
  LEFT JOIN IDEN_DOC_REF_REGISTRY passport ON passport.A_LD = person.A_OUID
    AND ISNULL(passport.A_STATUS, 10) = 10
