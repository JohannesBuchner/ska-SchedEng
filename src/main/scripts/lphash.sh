head -n1 $1 | grep '[0-9.]{10,}' -Eo|sort -un|md5sum|awk '{print $1}'
