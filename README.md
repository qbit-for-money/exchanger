exchanger
=========

####Развертывание приложения **exchanger** на хостинге **firstvds**

1. Установить **PostgreSQL** http://www.postgresql.org/download/linux/debian/
2. Зайти под пользователем **postgres**, для этого ввести команды:
  3. `su postgres`
  4. `psql`
5. Создать базы данных:
  6. `CREATE DATABASE test;`
7. Создать пользователя **redneckz**:
  8. `CREATE USER redneckz WITH password '*****';`
  9. `GRANT ALL privileges ON DATABASE test TO redneckz;`

10.  В **persistanse.xml** приложения, а также **exchanger.properties** настроить подключение под созданного пользователя.
11. Установить **jdk**, для этого ввести команды:
  12. `echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list`
  13. `echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list`
  14. `apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886`
  15. `apt-get update`
  16. `apt-get install oracle-java7-installer`
17. Добавить ssl-ключ **btc-e.com** в java http://magicmonster.com/kb/prg/java/ssl/pkix_path_building_failed.html
18. Установить  **Tomcat** http://firstwiki.ru/index.php/Apache_Tomcat
19. Для доступа к менежеру **Tomcat** завести пользователя **redneckz**.
Для этого необходимо изменить файл /etc/tomcat7/tomcat-users.xml: 
  20. `username="redneckz"`
  21. `password="*****"` 
  22. `roles="manager-gui,admin-gui"`

23. Создать почтовый ящик, например, **admin@bitgates.com**
Для этого зайти в **ispmgr** под пользователем и создать почтовый домен через меню **Почтовые домены**. Далее создать нужные почтовые ящики через меню **Почтовые ящики**.
24. Подписать сертификат **ssl**.
  25. Создать файл **keystore** https://support.comodo.com/index.php?_m=knowledgebase&_a=viewarticle&kbarticleid=244

  26. Заказать сертификат http://ru.ispdoc.com/index.php/Billmgr-certificate.order.1
На хостинге **firstvds** возможны два варианта запроса на сертификат. 
    27. Первый - мы используем сгенерированный нами ранее **Certificate Request**. Для этого вставляем его в соответствующее поле. Данный способ отказался работать. Вполне вероятно, что это временная проблема.
    28. Второй вариант - генерация **Certificate Request** самим **firstvds** при создании запроса на получение сертификата. 
Выбор почты, на которую мы хотим получить сертификат, возможен только из представленного списка, поэтому выбираем созданную ранее почту admin@bitgates.com .
  29. Добавить сертификат в keystore https://support.comodo.com/index.php?_m=knowledgebase&_a=viewarticle&kbarticleid=1204
Если при запросе сертификата был выбран второй способ, возникнет несоответствие сертификата и  Certificate Request в **keystore**. Решается http://www.agentbob.info/agentbob/79-AB.html

30. Перенастроить **Tomcat** на работу по https http://www.f-notes.info/tomcat:install_windows

31. Поскольку в **linux**  доступ java-приложениям к портам 80 и 443 по-умолчанию закрыт, и его лучше не открывать, **Tomcat** настраивается на порты 8080 и 8443.
Для перенаправления на данные порты внешнего пользователя был настроен **iptables** следующими командами:
  32. `iptables -A INPUT -i eth0 -p tcp --dport 80 -j ACCEPT`
  33. `iptables -A INPUT -i eth0 -p tcp --dport 8080 -j ACCEPT`
  34. `iptables -A PREROUTING -t nat -i eth0 -p tcp --dport 80 -j REDIRECT --to-port 8080`
  35. `iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-ports 8443`
  36. `iptables -t nat -A OUTPUT -p tcp --dport 443 -o lo -j REDIRECT --to-port 8443`

37. Настройка **iptables** после перезагрузки виртуальной машины сбрасывается. Поэтому необходимо вновь вводить данные команды.
Также необходим запуск **Tomcat**  в ручном режиме.

  38. старт: `/etc/init.d/tomcat7 start`
  39. стоп: `/etc/init.d/tomcat7 stop`
  40. перезапуск: `/etc/init.d/tomcat7 restart`

41. Для функционирования приложения необходимо установить права на запись в файлы в папках **bitcoin-wallet**, **litecoin-wallet**.

42. Разместить **.war** в **/var/lib/tomcat7/webapps/**, дать имя **ROOT.war**, перезапустить **Tomcat**.
