import{a as o}from"./axios-upsvKRUO.js";import{g as a,s as t}from"./index-DyHKvFXx.js";const r=o.create(),c=e=>{const s=a("accessToken");return s?(e.headers.Authorization=`Bearer ${s}`,e):Promise.reject({response:{data:{error:"accessToken이 없음"}}})},n=e=>(console.log("failReq Err",e),Promise.reject(e)),i=async e=>{const s=await o.get("/api/user/access-token");return t("accessToken",s.data),e},u=async e=>{console.log("failRes 에러",e);const s=await o.get("/api/user/access-token");return t("accessToken",s.data),Promise.reject(e)};r.interceptors.request.use(c,n);r.interceptors.response.use(i,u);export{r as j};
