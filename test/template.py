#!//usr/bin/python
import requests

url = 'http://172.104.123.14:28081/template/list'
headers = {'username': '1', 'pass': '1'}
resp = requests.get(url, headers = headers)
print(resp.status_code)
