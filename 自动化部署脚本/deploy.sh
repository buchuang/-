echo "===========����git��ĿhappymmallĿ¼============="
cd /developer/git-repository/mmall
echo "==========git�л���֮��mmall-v1.0==============="
git checkout mmall-v1.0
echo "==================git fetch======================"
git fetch
echo "==================git pull======================"
git pull
echo "===========���벢������Ԫ����===================="
mvn clean package -Dmaven.test.skip=true
echo "============ɾ���ɵ�ROOT.war==================="
rm /developer/apache-tomcat-7.0.73/webapps/ROOT.war
echo "======�������������war����tomcat��-ROOT.war======="
cp /developer/git-repository/mmall/target/mmall.war /developer/apache-tomcat-7.0.73/webapps/ROOT.war
echo "============ɾ��tomcat�¾ɵ�ROOT�ļ���============="
rm -rf /developer/apache-tomcat-7.0.73/webapps/ROOT
echo "====================�ر�tomcat====================="
/developer/apache-tomcat-7.0.73/bin/shutdown.sh
echo "================sleep 10s========================="
for i in {1..10}
do
    echo $i"s"
    sleep 1s
done
echo "====================����tomcat====================="
/developer/apache-tomcat-7.0.73/bin/startup.sh