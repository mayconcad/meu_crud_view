ALTER TABLE public.parametrorepasse ADD COLUMN codbanco varchar(10) DEFAULT '001';
ALTER TABLE public.parametrorepasse ADD COLUMN descricaobanco varchar(50) DEFAULT 'Banco do Brasil';

--REMOÇÃO DA RESTRIÇÃO DE UNICIDADE DA MATRICULA DO RESPONSÁVEL E ADIÇÃO DE UMA RESTRIÇÃO DE UNICIDADE PARA OS MATRICULA E ATIVO DO RESPONSÁVEL
ALTER TABLE public.responsavel DROP CONSTRAINT responsavel_matriculafuncional_key;
ALTER TABLE public.responsavel ADD CONSTRAINT responsavel_matriculafuncional_ativo_key UNIQUE (matriculafuncional,ativo);

--REMOÇÃO DA RESTRIÇÃO DE UNICIDADE DO USERNAME DE USUÁRIO
ALTER TABLE public.usuario DROP CONSTRAINT user_username_key;
ALTER TABLE public.usuario ADD CONSTRAINT user_username_ativo_key UNIQUE (username,ativo);

--REMOÇÃO DA RESTRIÇÃO DE UNICIDADE DO USERNAME DE USUÁRIO
ALTER TABLE public.unidade DROP CONSTRAINT unidade_nome_key;
ALTER TABLE public.unidade ADD CONSTRAINT unidade_nome_ativo_key UNIQUE (nome,ativo);

--ADIÇÃO DE NOVAS COLUNAS PARA A TABELA DE RESPONSÁVEL
ALTER TABLE public.responsavel ADD COLUMN cep varchar(10);
ALTER TABLE public.responsavel ADD COLUMN operacao varchar(10);
ALTER TABLE public.responsavel ADD COLUMN datacadastrogcs date;

