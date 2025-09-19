                      LOC 50
000062 000005         DATA 5           ; vector length
                      LOC 100          ; vector A
000144 000001         DATA 1
000145 000002         DATA 2
000146 000003         DATA 3
000147 000004         DATA 4
000150 000005         DATA 5
                      LOC 200          ; vector B
000310 000000         DATA 0
000311 000002         DATA 2
000312 000004         DATA 4
000313 000006         DATA 6
000314 000010         DATA 8
                      LOC 300          ; store addresses of A and B
000454 000144         DATA 100
000455 000310         DATA 200
000456 120022         LDFR 0,0,50
000457 112014         VADD 0,0,300
                      LOC 400
000620 000012         DATA 10
000621 000003         DATA 3
000622 120020         LDFR 0,0,400     ; FR0 = int 10
000623 104021         FADD 0,0,401     ; FR0 = FR0 + 3
000624 122022         STFR 0,0,402     ; Store FR0 result
000625 000000         HLT
