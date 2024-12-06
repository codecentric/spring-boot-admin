---
sidebar_custom_props:
  icon: 'python'
---

# Pyctuator

You can easily integrate Spring Boot Admin with [Flask](https://flask.palletsprojects.com) or [FastAPI](https://fastapi.tiangolo.com/) Python applications using the [Pyctuator](https://github.com/SolarEdgeTech/pyctuator) project.

The following steps uses Flask, but other web frameworks are supported as well. See Pyctuator’s documentation for an updated list of supported frameworks and features.

1. Install the pyctuator package:  
```bash  
pip install pyctuator  
```
2. Enable pyctuator by pointing it to your Flask app and letting it know where Spring Boot Admin is running:  
```python title="app.py"
import os  
from flask import Flask  
from pyctuator.pyctuator import Pyctuator  
app_name = "Flask App with Pyctuator"  
app = Flask(app_name)  
@app.route("/")  
def hello():  
    return "Hello World!"  
Pyctuator(  
    app,  
    app_name,  
    app_url="http://example-app.com",  
    pyctuator_endpoint_url="http://example-app.com/pyctuator",  
    registration_url=os.getenv("SPRING_BOOT_ADMIN_URL")  
)  
app.run()  
```

For further details and examples, see Pyctuator’s [documentation](https://github.com/SolarEdgeTech/pyctuator/blob/master/README.md) and [examples](https://github.com/SolarEdgeTech/pyctuator/tree/master/examples).
