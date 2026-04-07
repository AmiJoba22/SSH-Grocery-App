--
-- PostgreSQL database dump
--

-- Dumped from database version 16.5
-- Dumped by pg_dump version 16.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: delivery_id_seq; Type: SEQUENCE; Schema: public; Owner: ssh_cloud_admin
--

CREATE SEQUENCE public.delivery_id_seq
    AS integer
    START WITH 800001
    INCREMENT BY 1
    MINVALUE 800001
    MAXVALUE 899999
    CACHE 1;


ALTER SEQUENCE public.delivery_id_seq OWNER TO ssh_cloud_admin;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: deliveries; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.deliveries (
    delivery_id integer DEFAULT nextval('public.delivery_id_seq'::regclass) NOT NULL,
    order_id integer,
    delivery_time timestamp without time zone
);


ALTER TABLE public.deliveries OWNER TO ssh_cloud_admin;

--
-- Name: house_id_seq; Type: SEQUENCE; Schema: public; Owner: ssh_cloud_admin
--

CREATE SEQUENCE public.house_id_seq
    AS integer
    START WITH 100001
    INCREMENT BY 1
    MINVALUE 100001
    MAXVALUE 199999
    CACHE 1;


ALTER SEQUENCE public.house_id_seq OWNER TO ssh_cloud_admin;

--
-- Name: houses; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.houses (
    house_id integer DEFAULT nextval('public.house_id_seq'::regclass) NOT NULL,
    address text
);


ALTER TABLE public.houses OWNER TO ssh_cloud_admin;

--
-- Name: item_id_seq; Type: SEQUENCE; Schema: public; Owner: ssh_cloud_admin
--

CREATE SEQUENCE public.item_id_seq
    AS integer
    START WITH 600001
    INCREMENT BY 1
    MINVALUE 600001
    MAXVALUE 699999
    CACHE 1;


ALTER SEQUENCE public.item_id_seq OWNER TO ssh_cloud_admin;

--
-- Name: items; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.items (
    item_id integer DEFAULT nextval('public.item_id_seq'::regclass) NOT NULL,
    sub_order_id integer,
    product_id integer,
    quantity integer,
    total_price numeric(6,2)
);


ALTER TABLE public.items OWNER TO ssh_cloud_admin;

--
-- Name: order_id_seq; Type: SEQUENCE; Schema: public; Owner: ssh_cloud_admin
--

CREATE SEQUENCE public.order_id_seq
    AS integer
    START WITH 400001
    INCREMENT BY 1
    MINVALUE 400001
    MAXVALUE 499999
    CACHE 1;


ALTER SEQUENCE public.order_id_seq OWNER TO ssh_cloud_admin;

--
-- Name: orders; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.orders (
    order_id integer DEFAULT nextval('public.order_id_seq'::regclass) NOT NULL,
    house_id integer,
    order_status text,
    order_cost numeric(6,2)
);


ALTER TABLE public.orders OWNER TO ssh_cloud_admin;

--
-- Name: payment_id_seq; Type: SEQUENCE; Schema: public; Owner: ssh_cloud_admin
--

CREATE SEQUENCE public.payment_id_seq
    AS integer
    START WITH 700001
    INCREMENT BY 1
    MINVALUE 700001
    MAXVALUE 799999
    CACHE 1;


ALTER SEQUENCE public.payment_id_seq OWNER TO ssh_cloud_admin;

--
-- Name: payments; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.payments (
    payment_id integer DEFAULT nextval('public.payment_id_seq'::regclass) NOT NULL,
    sub_order_id integer,
    payment_status text
);


ALTER TABLE public.payments OWNER TO ssh_cloud_admin;

--
-- Name: product_id_seq; Type: SEQUENCE; Schema: public; Owner: ssh_cloud_admin
--

CREATE SEQUENCE public.product_id_seq
    AS integer
    START WITH 300001
    INCREMENT BY 1
    MINVALUE 300001
    MAXVALUE 399999
    CACHE 1;


ALTER SEQUENCE public.product_id_seq OWNER TO ssh_cloud_admin;

--
-- Name: products; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.products (
    product_id integer DEFAULT nextval('public.product_id_seq'::regclass) NOT NULL,
    product_name text,
    product_price numeric(6,2),
    discount numeric(6,4),
    supplier_id integer,
    is_available boolean
);


ALTER TABLE public.products OWNER TO ssh_cloud_admin;

--
-- Name: sub_order_id_seq; Type: SEQUENCE; Schema: public; Owner: ssh_cloud_admin
--

CREATE SEQUENCE public.sub_order_id_seq
    AS integer
    START WITH 500001
    INCREMENT BY 1
    MINVALUE 500001
    MAXVALUE 599999
    CACHE 1;


ALTER SEQUENCE public.sub_order_id_seq OWNER TO ssh_cloud_admin;

--
-- Name: sub_orders; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.sub_orders (
    sub_order_id integer DEFAULT nextval('public.sub_order_id_seq'::regclass) NOT NULL,
    order_id integer,
    user_id uuid,
    sub_order_cost numeric(6,2)
);


ALTER TABLE public.sub_orders OWNER TO ssh_cloud_admin;

--
-- Name: supplier_id_seq; Type: SEQUENCE; Schema: public; Owner: ssh_cloud_admin
--

CREATE SEQUENCE public.supplier_id_seq
    AS integer
    START WITH 200001
    INCREMENT BY 1
    MINVALUE 200001
    MAXVALUE 299999
    CACHE 1;


ALTER SEQUENCE public.supplier_id_seq OWNER TO ssh_cloud_admin;

--
-- Name: suppliers; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.suppliers (
    supplier_id integer DEFAULT nextval('public.supplier_id_seq'::regclass) NOT NULL,
    supplier_name text
);


ALTER TABLE public.suppliers OWNER TO ssh_cloud_admin;

--
-- Name: users; Type: TABLE; Schema: public; Owner: ssh_cloud_admin
--

CREATE TABLE public.users (
    user_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_name text,
    house_id integer,
    user_password text,
    short_id text
);


ALTER TABLE public.users OWNER TO ssh_cloud_admin;

--
-- Data for Name: deliveries; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.deliveries (delivery_id, order_id, delivery_time) FROM stdin;
800002	400002	2024-02-28 08:33:28
800001	400001	2023-12-02 10:41:20
\.


--
-- Data for Name: houses; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.houses (house_id, address) FROM stdin;
100001	23 Bristol Road
100002	1 London Road
100003	196 Chapel Lane
100004	486 New Street
\.


--
-- Data for Name: items; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.items (item_id, sub_order_id, product_id, quantity, total_price) FROM stdin;
600001	500001	300003	2	0.70
600003	500002	300002	2	1.90
600005	500003	300005	1	4.50
600002	500001	300002	1	0.95
600004	500002	300006	1	2.00
\.


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.orders (order_id, house_id, order_status, order_cost) FROM stdin;
400003	100002	Created	0.00
400002	100002	Delivered	4.50
400001	100001	Delivered	5.55
\.


--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.payments (payment_id, sub_order_id, payment_status) FROM stdin;
700001	500001	Paid
700003	500003	To be paid
700002	500002	Paid
\.


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.products (product_id, product_name, product_price, discount, supplier_id, is_available) FROM stdin;
300008	Baileys Original Irish Cream Liqueur 1L	21.95	0.4556	200002	f
300002	Essential Iceburg Lettuce	0.95	0.0000	200001	t
300004	Essential Sweetcorn	1.50	0.0000	200001	t
300006	Waitrose Green Seedless Grapes	2.40	0.8333	200001	t
300007	Waitrose Strawberries	2.70	0.0000	200001	f
300003	Essential Lemons	0.35	0.0000	200001	t
300005	Gressingham Corn Fed Poussin	4.50	0.0000	200001	t
300001	Essential Broccoli	1.00	0.0000	200001	t
\.


--
-- Data for Name: sub_orders; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.sub_orders (sub_order_id, order_id, user_id, sub_order_cost) FROM stdin;
500001	400001	a2c02086-814f-4333-b0d9-02a775560831	1.65
500003	400002	9f87f607-a2b3-4f95-8e40-720f5a5f2a6c	4.50
500002	400001	498c3b11-9dac-4e7a-badd-9b985dc4bf6c	3.90
500004	400003	af9fb1e6-7df1-4db7-9436-53c9db6861df	0.00
\.


--
-- Data for Name: suppliers; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.suppliers (supplier_id, supplier_name) FROM stdin;
200005	Lidl
200003	Tesco
200001	Waitrose
200004	Aldi
200002	Sainsbury's
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: ssh_cloud_admin
--

COPY public.users (user_id, user_name, house_id, user_password, short_id) FROM stdin;
a2c02086-814f-4333-b0d9-02a775560831	Jacky Chan	100001	password	\N
498c3b11-9dac-4e7a-badd-9b985dc4bf6c	Charlie	100001	pass123	\N
9f87f607-a2b3-4f95-8e40-720f5a5f2a6c	Emily Yang	100002	123456789	\N
af9fb1e6-7df1-4db7-9436-53c9db6861df	Louise	100002	ThisIsPassword	\N
0f61bfde-f67f-4f8c-81f9-bcab3974c216	Billy Jeans	100003	iLoveCS	\N
715d3d1e-0064-4556-9259-69d3b9b7c0e3	user1	100001	cGFzc3dvcmQ=	user1_94a35d87
\.


--
-- Name: delivery_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ssh_cloud_admin
--

SELECT pg_catalog.setval('public.delivery_id_seq', 800002, true);


--
-- Name: house_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ssh_cloud_admin
--

SELECT pg_catalog.setval('public.house_id_seq', 100004, true);


--
-- Name: item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ssh_cloud_admin
--

SELECT pg_catalog.setval('public.item_id_seq', 600005, true);


--
-- Name: order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ssh_cloud_admin
--

SELECT pg_catalog.setval('public.order_id_seq', 400003, true);


--
-- Name: payment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ssh_cloud_admin
--

SELECT pg_catalog.setval('public.payment_id_seq', 700003, true);


--
-- Name: product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ssh_cloud_admin
--

SELECT pg_catalog.setval('public.product_id_seq', 300008, true);


--
-- Name: sub_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ssh_cloud_admin
--

SELECT pg_catalog.setval('public.sub_order_id_seq', 500004, true);


--
-- Name: supplier_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ssh_cloud_admin
--

SELECT pg_catalog.setval('public.supplier_id_seq', 200005, true);


--
-- Name: deliveries deliveries_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.deliveries
    ADD CONSTRAINT deliveries_pkey PRIMARY KEY (delivery_id);


--
-- Name: houses houses_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.houses
    ADD CONSTRAINT houses_pkey PRIMARY KEY (house_id);


--
-- Name: items items_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.items
    ADD CONSTRAINT items_pkey PRIMARY KEY (item_id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (order_id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (payment_id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (product_id);


--
-- Name: sub_orders sub_orders_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.sub_orders
    ADD CONSTRAINT sub_orders_pkey PRIMARY KEY (sub_order_id);


--
-- Name: suppliers suppliers_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_pkey PRIMARY KEY (supplier_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: deliveries deliveries_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.deliveries
    ADD CONSTRAINT deliveries_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: deliveries deliveries_order_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.deliveries
    ADD CONSTRAINT deliveries_order_id_fkey1 FOREIGN KEY (order_id) REFERENCES public.orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: items items_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.items
    ADD CONSTRAINT items_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(product_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: items items_sub_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.items
    ADD CONSTRAINT items_sub_order_id_fkey FOREIGN KEY (sub_order_id) REFERENCES public.sub_orders(sub_order_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: orders orders_house_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_house_id_fkey FOREIGN KEY (house_id) REFERENCES public.houses(house_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: orders orders_house_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_house_id_fkey1 FOREIGN KEY (house_id) REFERENCES public.houses(house_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: payments payments_sub_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_sub_order_id_fkey FOREIGN KEY (sub_order_id) REFERENCES public.sub_orders(sub_order_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: products products_supplier_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES public.suppliers(supplier_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: products products_supplier_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_supplier_id_fkey1 FOREIGN KEY (supplier_id) REFERENCES public.suppliers(supplier_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: sub_orders sub_orders_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.sub_orders
    ADD CONSTRAINT sub_orders_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: sub_orders sub_orders_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.sub_orders
    ADD CONSTRAINT sub_orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: users users_house_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_house_id_fkey FOREIGN KEY (house_id) REFERENCES public.houses(house_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- Name: users users_house_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: ssh_cloud_admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_house_id_fkey1 FOREIGN KEY (house_id) REFERENCES public.houses(house_id) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- PostgreSQL database dump complete
--

