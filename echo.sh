declare -i a=0
declare -i b=1
sh compile.sh
while true
do
    sh run.sh > "twitter_trends+$a.txt"
    a=$(( a + b ))
    sleep 600
done
