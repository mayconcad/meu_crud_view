CREATE TABLE public.role (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(60) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE public.user (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  password varchar(10) NOT NULL,
  username varchar(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY username (username)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

CREATE TABLE public.user_role (
  User_id bigint(20) NOT NULL,
  roles_id bigint(20) NOT NULL,
  KEY FK8B9F886AF2EA7695 (User_id),
  KEY FK8B9F886AEBD82B0E (roles_id),
  CONSTRAINT FK8B9F886AEBD82B0E FOREIGN KEY (roles_id) REFERENCES role (id),
  CONSTRAINT FK8B9F886AF2EA7695 FOREIGN KEY (User_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--ALTER TABLE public.user RENAME TO "usuario"
--ALTER TABLE public.user_role RENAME TO "usuario_role"
--ALTER TABLE public.usuario_role RENAME COLUMN user_id TO usuario_id

INSERT INTO public.user (id, ativo, createdat, email, name, password, username) VALUES (1, true, '2015-03-25', 'admin@gmail.com', 'Administrador', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3','admin');
INSERT INTO public.user (id, ativo, createdat, email, name, password, username) VALUES (2,true, '2015-03-25', 'gestor@gmail.com', 'Gestor','0834c2d60725ac5902257b3b78dd161ad26d1c0290dbf1e47cc14add5b8c8142','gestor');  
INSERT INTO public.user (id, ativo, createdat, email, name, password, username) VALUES (3,true, '2015-03-25', 'responsavel@gmail.com', 'Responsável','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','responsavel');

INSERT INTO public.role (id, ativo, name) VALUES (1,true, 'ADMIN');
INSERT INTO public.role (id, ativo, name) VALUES (2,true, 'GESTOR');
INSERT INTO public.role (id, ativo, name) VALUES (3,true, 'RESPONSAVEL');

INSERT INTO public.user_role (User_id, roles_id) VALUES (1,1);
INSERT INTO public.user_role (User_id, roles_id) VALUES (2,2);
INSERT INTO public.user_role (User_id, roles_id) VALUES (3,3);
