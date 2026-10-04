from pathlib import Path
import re,json,tomllib,xml.etree.ElementTree as E
p=Path(__file__).resolve().parents[1];errors=[];checks=[]
ns={'a':'http://schemas.android.com/apk/res/android'};A='{http://schemas.android.com/apk/res/android}'
resources=p/'app/src/main/res'
xmls=list(resources.rglob('*.xml'))+[p/'app/src/main/AndroidManifest.xml']
for f in xmls:
 try:E.parse(f)
 except E.ParseError as e:errors.append(str(f)+': '+str(e))
checks.append(f'XML bien formado: {len(xmls)} archivos')
strings={n.attrib['name'] for n in E.parse(resources/'values/strings.xml').getroot()}
es={n.attrib['name'] for n in E.parse(resources/'values-es/strings.xml').getroot()}
if strings!=es:errors.append('Claves ES/EN diferentes')
strings.add('google_web_client_id')
checks.append(f'Paridad de recursos ES/EN: {len(es)} claves')
ids=set();layouts={f.stem for f in (resources/'layout').glob('*.xml')};drawables={f.stem for f in (resources/'drawable').glob('*.xml')}
for f in xmls:
 ids.update(re.findall(r'@\+id/([A-Za-z_0-9]+)',f.read_text()))
code='\n'.join(f.read_text() for f in (p/'app/src/main/java').rglob('*.kt'))
for typ,known in [('string',strings),('id',ids),('layout',layouts),('drawable',drawables)]:
 for name in re.findall(r'(?<!android\.)R\.'+typ+r'\.([A-Za-z_0-9]+)',code):
  if name not in known:errors.append(f'Recurso no resuelto: R.{typ}.{name}')
for f in xmls:
 for typ,name in re.findall(r'@(string|layout|drawable)/([A-Za-z_0-9]+)',f.read_text()):
  if name not in {'string':strings,'layout':layouts,'drawable':drawables}[typ]:errors.append(f'{f.name}: @{typ}/{name}')
checks.append('Referencias locales de strings, IDs, layouts y drawables')
for f in (p/'app/src/main/java').rglob('*.kt'):
 package=re.search(r'^package ([\w.]+)',f.read_text()).group(1)
 if f.parent.relative_to(p/'app/src/main/java').as_posix()!=package.replace('.','/'):errors.append('Ruta/paquete diferente: '+str(f))
checks.append('Paquetes Kotlin alineados con sus rutas')
for frag in E.parse(resources/'navigation/nav_graph.xml').getroot():
 name=frag.attrib.get(A+'name','').split('.')[-1]
 if name and not re.search(r'class\s+'+name+r'\b',code):errors.append('Fragment sin clase: '+name)
checks.append('Destinos Navigation con clases presentes')
for name in re.findall(r'\b((?:Fragment|Activity|Item)\w+Binding)\b',code):
 layout=re.sub(r'(?<!^)(?=[A-Z])','_',name[:-7]).lower()
 if layout not in layouts:errors.append('Binding sin layout: '+name)
checks.append('Clases ViewBinding con sus layouts')
for f in [p/'package.json',p/'firebase.json',p/'firestore.indexes.json']:json.loads(f.read_text())
catalog=tomllib.loads((p/'gradle/libs.versions.toml').read_text())
for alias in re.findall(r'libs\.([a-z][a-z.]*)',(p/'app/build.gradle.kts').read_text()):
 if alias=='plugins.android.application':continue
 if alias.replace('.','-') not in catalog['libraries']:errors.append('Alias Gradle faltante: '+alias)
checks.append('JSON y TOML válidos; dependencias con alias existentes')
if 'com.example' in code:errors.append('Quedan paquetes antiguos')
if 'findViewById' in code or 'notifyDataSetChanged' in code:errors.append('Quedan mecanismos antiguos de vistas/listado')
checks.append('Sin imports del paquete anterior, findViewById ni notifyDataSetChanged')
report={'checks':checks,'errors':sorted(set(errors)),'kotlin_files':len(list((p/'app/src/main/java').rglob('*.kt'))),'unit_tests_authored':sum(f.read_text().count('@Test') for f in (p/'app/src/test').rglob('*.kt')),'instrumentation_tests_authored':sum(f.read_text().count('@Test') for f in (p/'app/src/androidTest').rglob('*.kt')),'rules_tests_authored':(p/'security-tests/firestore.test.mjs').read_text().count("test('")}
(p/'docs').mkdir(exist_ok=True);(p/'docs/static-checks.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(report,ensure_ascii=False,indent=2));raise SystemExit(bool(errors))
