import os, requests
BASE="https://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
def search_pubmed(query):
    if len(query.strip())<2: raise ValueError("Search query must contain at least two characters.")
    common={"db":"pubmed","term":query.strip(),"retmode":"json","retmax":10,"tool":os.getenv("NCBI_TOOL","HealthSkillConnectDemo"),"email":os.getenv("NCBI_EMAIL","demo@example.com")}
    try:
        r=requests.get(BASE+"esearch.fcgi",params=common,timeout=8);r.raise_for_status();ids=r.json().get("esearchresult",{}).get("idlist",[])
        if not ids:return {"query":query,"count":0,"articles":[],"disclaimer":"Literature discovery only, not medical advice."}
        params={"db":"pubmed","id":",".join(ids),"retmode":"json","tool":common["tool"],"email":common["email"]}
        r=requests.get(BASE+"esummary.fcgi",params=params,timeout=8);r.raise_for_status();data=r.json().get("result",{})
        articles=[]
        for pmid in ids:
            item=data.get(pmid,{})
            if item: articles.append({"pmid":pmid,"title":item.get("title","Title unavailable"),"publicationDate":item.get("pubdate"),"source":item.get("fulljournalname"),"url":f"https://pubmed.ncbi.nlm.nih.gov/{pmid}/"})
        return {"query":query,"count":len(articles),"articles":articles,"disclaimer":"Literature discovery only, not medical advice. Check the article and current evidence before relying on it."}
    except requests.RequestException as e: raise RuntimeError("PubMed could not be reached. Please retry later.") from e
