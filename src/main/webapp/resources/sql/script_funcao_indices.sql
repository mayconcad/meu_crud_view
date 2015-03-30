-- Verifica e cria novo índice se for o caso
CREATE OR REPLACE FUNCTION criaindex (tabela varchar, campo varchar) RETURNS VARCHAR AS $$
DECLARE
func_cmd VARCHAR;
BEGIN
if retindex($1,$2) > 0 then
RETURN 'OK';
else
func_cmd := 'CREATE INDEX ' || $1 || '_IDX ON ' || $1 || ' (' || $2 || ')';
EXECUTE func_cmd;
RETURN func_cmd;
end if;
END;
$$ LANGUAGE plpgsql;