#!/usr/bin/env python3
"""Redacted heuristic scan of working files and optional reachable history."""
import base64,json,re,subprocess,sys
from pathlib import Path
patterns={
 'private-key':rb'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----',
 'privileged-key':rb'sb_secret_[A-Za-z0-9_-]{16,}',
 'github-token':rb'(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{40,})',
 'google-key-review':rb'AIza[A-Za-z0-9_-]{35}',
 'aws-key':rb'AKIA[0-9A-Z]{16}',
 'database-password':rb'postgres(?:ql)?://[^\s:@]+:[^\s@]{6,}@'}
def findings(data):
 result={name for name,p in patterns.items() if re.search(p,data)}
 for m in re.finditer(rb'eyJ[A-Za-z0-9_-]+\.([A-Za-z0-9_-]+)\.[A-Za-z0-9_-]+',data):
  try:
   if json.loads(base64.urlsafe_b64decode(m[1]+b'='*(-len(m[1])%4))).get('role')=='service_role':result.add('service-role-jwt')
  except (ValueError,TypeError):pass
 return result
errors=[]
for n in filter(None,subprocess.check_output(['git','ls-files','-z','--cached','--others','--exclude-standard']).split(b'\0')):
 p=Path(n.decode())
 if p.is_file():errors.extend(f'{p}: {label}' for label in findings(p.read_bytes()))
if '--history' in sys.argv:
 for row in subprocess.check_output(['git','rev-list','--objects','--all']).splitlines():
  oid,_,path=row.partition(b' ')
  if not path or subprocess.check_output(['git','cat-file','-t',oid]).strip()!=b'blob':continue
  errors.extend(f'history {oid[:12].decode()} {path.decode(errors="replace")}: {label}' for label in findings(subprocess.check_output(['git','cat-file','blob',oid])))
for e in errors:print(e)
print(f'Secret-pattern scan: {len(errors)} findings (values redacted). Not an exhaustive audit.')
sys.exit(bool(errors))
