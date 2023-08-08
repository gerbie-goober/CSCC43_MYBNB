USE cscc43_mybnb;

DROP TABLE IF EXISTS Records, Listings, Users, Ratings;

CREATE TABLE Listings (
    LID integer AUTO_INCREMENT not null primary key,
    type varchar(30) not null,
    latitude float not null,
    longitude float not null,
    street varchar(60),
    postal_code varchar(60),
    city varchar(30),
    country varchar(30),
    amenities varchar(100),
    UNIQUE INDEX idx_unique_location (street, postal_code, city, country)
);

CREATE TABLE Users(
	SIN integer,
	role varchar(8) not null,
	first_name varchar(25) not null,
	last_name varchar(25) not null,
	address varchar(40),
	DOB varchar(11),
	occupation varchar(25),
	credit_card_info varchar(50),
	primary key (SIN, role)
);

CREATE TABLE Records(
    RID integer AUTO_INCREMENT not null primary key,
	price float not null,
	date_avail varchar(11) not null,
	is_unavail boolean,
	is_booked boolean,
	is_cancelled boolean,
	SIN integer,
	LID integer,
	role varchar(8),
	foreign key (SIN, role) references Users (SIN, role),
	foreign key (LID) references Listings (LID)
);

CREATE TABLE Ratings (
    SIN_writer INTEGER,
    role_writer VARCHAR(8),
    SIN_receiver INTEGER,
    role_receiver VARCHAR(8),
    LID INTEGER,
    comments TEXT,
    listings_scale INTEGER CHECK (listings_scale >= 0 AND listings_scale <= 5),
    host_scale INTEGER CHECK (host_scale >= 0 AND host_scale <= 5),
    renter_scale INTEGER CHECK (renter_scale >= 0 AND renter_scale <= 5),
    is_reply BOOLEAN,
    PRIMARY KEY (SIN_writer, role_writer, SIN_receiver, role_receiver, is_reply),
    FOREIGN KEY (LID) REFERENCES Listings(LID),
    FOREIGN KEY (SIN_writer, role_writer) REFERENCES Users(SIN, role),
    FOREIGN KEY (SIN_receiver, role_receiver) REFERENCES Users(SIN, role)
);

insert into Users values (100000000, 'renter', 'bill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (100000001, 'renter', 'shill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (100000011, 'renter', 'crill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (100000111, 'renter', 'mill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (100001111, 'renter', 'till', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (100011111, 'renter', 'rill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');

insert into Users values (110000000, 'host', 'fill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (110000001, 'host', 'till', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (111000011, 'host', 'bill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (111100111, 'host', 'mill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (111101111, 'host', 'yill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');
insert into Users values (111111111, 'host', 'lill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');

insert into Users values (100000000, 'host', 'bill', 'bye', 'some address', '05/04/2003', 'farmer', '123123283');

INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities)
VALUES ('Apartment', 41.4522, 2.2477, '123 Sesame Street', 'some postal code', 'not some city', 'some country', 'lots of amenities');

INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities)
VALUES ('Full House', 41.45079, 1.000, '456 Sesame Street','some postal code', 'some city', 'some country', 'lots of amenities');

INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities)
VALUES ('Room', 1000.029, 1000.028, '789 Sesame Street','some postal code', 'some city', 'some country', 'lots of amenities');

INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities)
VALUES ('Apartment', 1000.029, 1000.028, '101 Sesame Street', 'some postal code', 'some city', 'some country', 'lots of amenities');

INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities)
VALUES ('Apartment', 40.7128, -74.0060, '123 Main St', '10001', 'New York City', 'USA', 'Wi-Fi, Pool');

INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities)
VALUES ('Apartment', 40.7128, -72.081, '578 Main St', '10002', 'New York City', 'USA', 'Wi-Fi, Pool');

INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities)
VALUES ('Apartment', 38.1098, -70.1283, 'Cool Guy Apartment', '10587', 'New York City', 'USA', 'A/C, Wi-Fi, Rooftop access');

INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities)
VALUES ('Full House', 1000.029, -1000.028, '19283 Some Cool Address','some postal code', 'some city', 'unique country', 'lots of amenities and money');


insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/20/2023', false, false, false, 110000000, 1, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/21/2023', false, false, false, 110000000, 1, 'host');

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/22/2023', false, false, false, 110000000, 2, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2023', false, false, false, 110000000, 2, 'host');

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2022', false, false, false, 110000000, 2, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/24/2022', false, false, false, 110000000, 2, 'host');

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('1000.00', '07/01/2023', false, false, false, 110000000, 7, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('1000.00', '07/02/2023', false, false, false, 110000000, 7, 'host');

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('10000.00', '12/01/2023', false, false, false, 111100111, 8, 'host');

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/20/2023', false, false, false, 110000001, 3, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/21/2023', false, false, false, 110000001, 3, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/22/2023', false, false, false, 110000001, 4, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2023', false, false, false, 110000001, 4, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2023', false, false, false, 110000001, 5, 'host');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2023', false, false, false, 110000001, 6, 'host');

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2023', false, true, false, 100000000, 5, 'renter');
update Records set is_booked=true WHERE LID=5 AND SIN=110000001 AND role='host' AND date_avail='05/23/2023';

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2023', false, true, false, 100000011, 6, 'renter');
update Records set is_booked=true WHERE LID=6 AND SIN=110000001 AND role='host' AND date_avail='05/23/2023';
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2022', false, true, false, 100000011, 2, 'renter');
update Records set is_booked=true WHERE LID=2 AND SIN=110000000 AND role='host' AND date_avail='05/23/2022';
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('10000.00', '12/01/2023', false, true, false, 100000011, 8, 'renter');
update Records set is_booked=true WHERE LID=8 AND SIN=111100111 AND role='host' AND date_avail='12/01/2023';

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/23/2023', false, true, false, 100011111, 2, 'renter');
insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/22/2023', false, true, false, 100011111, 2, 'renter');
update Records set is_booked=true WHERE LID=2 AND SIN=110000000 AND role='host' AND date_avail='05/23/2023';
update Records set is_booked=true WHERE LID=2 AND SIN=110000000 AND role='host' AND date_avail='05/22/2023';

insert into Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) values ('100.00', '05/24/2022', false, true, false, 100000000, 2, 'renter');
update Records set is_booked=true WHERE LID=2 AND SIN=110000000 AND role='host' AND date_avail='05/24/2022';

update Records set is_cancelled=true WHERE LID=2 AND SIN=110000000 AND role='host' AND date_avail='05/23/2022' AND is_booked=true;
update Records set is_cancelled=true WHERE LID=2 AND SIN=100000000 AND role='renter' AND date_avail='05/24/2022' AND is_booked=true;

update Records set is_cancelled=true WHERE LID=6 AND SIN=100000011 AND role='renter' AND date_avail='05/23/2023' AND is_booked=true;
update Records set is_cancelled=true WHERE LID=8 AND SIN=100000011 AND role='renter' AND date_avail='12/01/2023' AND is_booked=true;

update Records set is_cancelled=true WHERE LID=5 AND SIN=100000000 AND role='renter' AND date_avail='05/23/2023' AND is_booked=true;

insert into ratings values (100011111, 'renter', 110000000, 'host', 2, 'The room was very clean when I entered. I love how spacious the dining room. Our family really utilized it, and we made many delicious meals.', 5, 5, 0, false);
insert into ratings values (110000000, 'host', 100011111, 'renter', 2, 'The room was cleaned and restored back to original condition after they left. They were repectful of the property and neighborhood.', 0, 0, 5, false);

insert into ratings values (100000011, 'renter', 110000001, 'host', 6, 'The host tried to upcharge the listing and the date that I booked last minute, but I refused because that was against MyBnB policies. He was rude and pressuing.', 1, 1, 0, false);
insert into ratings values (110000001, 'host', 100000011, 'renter', 6, 'I am sorry to hear that you felt that I was rude. There was a financial bottleneck, and for any future bookers, please know that I can be very accomdating.', 0, 0, 3, true);



