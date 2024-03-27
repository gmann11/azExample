from neo4j import GraphDatabase, custom_auth

driver = GraphDatabase.driver("bolt://localhost:7687")
print ("db",driver)

cst_auth = custom_auth(principal='neo4j', credentials='password', realm=None, scheme="basic", parameters={"param1":"test1","param2":"test2"})
with driver.session(database="dpaa", auth=cst_auth) as session:
  result = session.run("RETURN true")
  print ("result", result.single())

