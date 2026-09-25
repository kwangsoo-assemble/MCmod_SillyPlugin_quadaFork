package dev.celestial.silly.helper;

import dev.celestial.silly.SillyPlugin;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.*;

import java.util.*;

public class SillyScriptVisitor extends Visitor {
    public Deque<SillyToken> token_stack = new ArrayDeque<>();
    public SillyToken current;
    public SillyScriptVisitor() {
        super();
    }

    public void enter(SillyToken tok) {
        add(tok);
        token_stack.push(tok);
        current = tok;
        SillyPlugin.LOGGER.info("Enter token {}", tok.name);
    }

    public void enter(String name) {
        enter(new SillyToken(name));
    }

    public void add(SillyToken tok) {
        if (token_stack.peek() != null) {
            token_stack.peek().children.add(tok);
        }
    }

    public void exit() {
        SillyPlugin.LOGGER.info("Exit token {}", token_stack.peek().name);
        token_stack.pop();
        current = token_stack.peek();
    }

    @Override
    public void visit(Chunk chunk) {
        enter(SillyToken.fromSL("chunk", chunk)); {
            super.visit(chunk);
        }
    }

    @Override
    public void visit(Block block) {
        enter(SillyToken.fromSL("block", block)); {
            super.visit(block);
            exit();
        }
    }

    @Override
    public void visit(Stat.Assign stat) {
        enter(SillyToken.fromSL("assign", stat)); {
            enter(new SillyToken("vars")); {
                visitVars(stat.vars);
                exit();
            }
            enter(new SillyToken("exps")); {
                visitExps(stat.exps);
                exit();
            }
            exit();
        }
    }

    @Override
    public void visit(Stat.Break breakstat) {
        enter(SillyToken.fromSL("break", breakstat)); {
            super.visit(breakstat);
            exit();
        }
    }

    @Override
    public void visit(Stat.FuncCallStat stat) {
        enter(SillyToken.fromSL("func_call_stat", stat)); {
            super.visit(stat);
            exit();
        }
    }

    @Override
    public void visit(Stat.FuncDef stat) {
        enter(SillyToken.fromSL("func_def", stat)); {
            enter(SillyToken.fromSL("func_name", stat.name)); {
                if (stat.name.name != null)
                    current.setProp("name", stat.name.name.name);
                if (stat.name.method != null)
                    current.setProp("method", stat.name.method);
                if (stat.name.dots != null && !stat.name.dots.isEmpty()) {
                    var table = new LuaTable();
                    for (int i = 0; i < stat.name.dots.size(); i++) {
                        table.set(i + 1, stat.name.dots.get(i));
                    }
                    current.setProp("dots", table);
                }
                exit();
            }
            super.visit(stat);
            exit();
        }
    }

    @Override
    public void visit(Stat.GenericFor stat) {
        enter(SillyToken.fromSL("generic_for", stat)); {
            enter(new SillyToken("scope")); {
                enter(new SillyToken("names")); {
                    visitNames(stat.names);
                    exit();
                }
                enter("exps"); {
                    visitExps(stat.exps);
                    exit();
                }
                stat.block.accept(this);
                exit();
            }
            exit();
        }
    }

    @Override
    public void visit(Stat.IfThenElse stat) {
        enter(SillyToken.fromSL("if_then_else", stat)); {
            enter("if"); {
                stat.ifexp.accept(this);
                stat.ifblock.accept(this);
                exit();
            }
            if (stat.elseifblocks != null)
                for (int i = 0, n = stat.elseifblocks.size(); i < n; i++) {
                    enter("elseif"); {
                        stat.elseifexps.get(i).accept(this);
                        stat.elseifblocks.get(i).accept(this);
                        exit();
                    }
                }
            if (stat.elseblock != null) {
                enter("else"); {
                    visit(stat.elseblock);
                    exit();
                }
            }
            exit();
        }
    }

    @Override
    public void visit(Stat.LocalAssign stat) {
        enter(SillyToken.fromSL("local_assign", stat)); {
            enter("names"); {
                visitNames(stat.names);
                exit();
            }
            enter("values"); {
                visitExps(stat.values);
                exit();
            }
            exit();
        }
    }

    @Override
    public void visit(Stat.LocalFuncDef stat) {
        enter(SillyToken.fromSL("local_func_def", stat)); {
            super.visit(stat);
            exit();
        }
    }

    @Override
    public void visit(Stat.NumericFor stat) {
        enter(SillyToken.fromSL("numeric_for", stat)); {
            visit(stat.scope);
            visit(stat.name);
            stat.initial.accept(this);
            stat.limit.accept(this);
            if (stat.step != null)
                stat.step.accept(this);
            stat.block.accept(this);
            exit();
        }
    }

    @Override
    public void visit(Stat.RepeatUntil stat) {
        enter(SillyToken.fromSL("repeat_until", stat)); {
            super.visit(stat);
            exit();
        }
    }

    @Override
    public void visit(Stat.Return stat) {
        enter(SillyToken.fromSL("return", stat)); {
            enter("values"); {
                super.visit(stat);
                exit();
            }
            exit();
        }
    }

    @Override
    public void visit(Stat.WhileDo stat) {
        enter(SillyToken.fromSL("while_do", stat)); {
            super.visit(stat);
            exit();
        }
    }

    @Override
    public void visit(FuncBody body) {
        enter(SillyToken.fromSL("func_body", body)); {
            super.visit(body);
            exit();
        }
    }

    @Override
    public void visit(FuncArgs args) {
        enter(SillyToken.fromSL("func_args", args)); {
            super.visit(args);
            exit();
        }
    }

    @Override
    public void visit(TableField field) {
        enter(SillyToken.fromSL("table_field", field)); {
            enter("key"); {
                if (field.name != null)
                    visit(field.name);
                if (field.index != null)
                    field.index.accept(this);
                exit();
            }
            enter("value"); {
                field.rhs.accept(this);
                exit();
            }
            exit();
        }
    }

    @Override
    public void visit(Exp.AnonFuncDef exp) {
        enter(SillyToken.fromSL("anon_func_def", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.BinopExp exp) {
        enter(SillyToken.fromSL("binary_op", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.Constant exp) {
        enter(SillyToken.fromSL("constant", exp)); {
            current.setProp("value", exp.value);
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.FieldExp exp) {
        enter(SillyToken.fromSL("field_exp", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.FuncCall exp) {
        enter(SillyToken.fromSL("func_call", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.IndexExp exp) {
        enter(SillyToken.fromSL("index", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.MethodCall exp) {
        enter(SillyToken.fromSL("method_call", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.NameExp exp) {
        enter(SillyToken.fromSL("name", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.ParensExp exp) {
        enter(SillyToken.fromSL("parens", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.UnopExp exp) {
        enter(SillyToken.fromSL("unary_op", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(Exp.VarargsExp exp) {
        enter(SillyToken.fromSL("varargs", exp)); {
            super.visit(exp);
            exit();
        }
    }

    @Override
    public void visit(ParList pars) {
        enter(SillyToken.fromSL("par_list", pars)); {
            current.setProp("is_vararg", pars.isvararg);
            super.visit(pars);
            exit();
        }
    }

    @Override
    public void visit(TableConstructor table) {
        enter(SillyToken.fromSL("table_constructor", table)); {
            super.visit(table);
            exit();
        }
    }

    @Override
    public void visitVars(List<Exp.VarExp> vars) {
        super.visitVars(vars);
    }

    @Override
    public void visitExps(List<Exp> exps) {
        super.visitExps(exps);
    }

    @Override
    public void visitNames(List<Name> names) {
        super.visitNames(names);
    }

    @Override
    public void visit(Name name) {
        enter("name"); {
            current.setProp("name", name.name);
            visit(name.variable);
            super.visit(name);
            exit();
        }
    }

    public void visit(Variable var) {
        if (var == null) return;
        enter("variable"); {
            current.setProp("name", var.name);
            current.setProp("has_assignments", var.hasassignments);
            if (var.initialValue != null)
                current.setProp("initial_value", var.initialValue);
            current.setProp("upvalue", var.isupvalue);
            current.setProp("constant", var.isConstant());
            current.setProp("local", var.isLocal());
            exit();
        }
    }

    @Override
    public void visit(String name) {
        enter("string"); {
            current.setProp("value", name);
            super.visit(name);
            exit();
        }
    }

    @Override
    public void visit(NameScope scope) {
        enter("name_scope"); {
            current.setProp("function_nesting_count", scope.functionNestingCount);
            var table = new LuaTable();
            for (var entry : scope.namedVariables.entrySet()) {
                var varTable = new LuaTable();
                var var = entry.getValue();
                if (var == null) {
                    SillyPlugin.LOGGER.info("Variable {} was null (???)", entry.getKey());
                    continue;
                }
                varTable.set("name", var.name);
                varTable.set("has_assignments", LuaValue.valueOf(var.hasassignments));
                if (var.initialValue != null)
                    varTable.set("initial_value", var.initialValue);
                varTable.set("upvalue", LuaValue.valueOf(var.isupvalue));
                varTable.set("constant", LuaValue.valueOf(var.isConstant()));
                varTable.set("local", LuaValue.valueOf(var.isLocal()));
                table.set(entry.getKey(), varTable);
            }
            current.setProp("named_variables", table);
            super.visit(scope);
            exit();
        }
    }

    @Override
    public void visit(Stat.Goto gotostat) {
        enter(SillyToken.fromSL("goto", gotostat)); {
            current.setProp("name", gotostat.name);
            super.visit(gotostat);
            exit();
        }
    }

    @Override
    public void visit(Stat.Label label) {
        enter(SillyToken.fromSL("label", label)); {
            current.setProp("name", label.name);
            super.visit(label);
            exit();
        }
    }

    public LuaTable toTable() {
        return Objects.requireNonNull(token_stack.peekLast()).toTable();
    }

    public static class SillyToken {
        public String name;
        public List<SillyToken> children = new ArrayList<>();
        public HashMap<String, LuaValue> properties = new HashMap<>();

        public SillyToken(String name) {
            this.name = name;
        }

        public static SillyToken fromSL(String name, SyntaxElement el) {
            var tok = new SillyToken(name);
            tok.setProp("line_begin", el.beginLine);
            tok.setProp("line_end", el.endLine);
            tok.setProp("column_begin", el.beginColumn);
            tok.setProp("column_end", el.endColumn);

            return tok;
        }

        public SillyToken setProp(String name, LuaValue value) {
            if (value == null) value = LuaValue.NIL;
            properties.put(name, value);
            return this;
        }

        public SillyToken setProp(String name, String value) {
            if (value == null) return this;
            return setProp(name, LuaValue.valueOf(value));
        }

        public SillyToken setProp(String name, Number value) {
            if (value == null) return this;
            return setProp(name, LuaValue.valueOf(value.intValue()));
        }

        public SillyToken setProp(String name, Boolean value) {
            if (value == null) return this;
            return setProp(name, LuaValue.valueOf(value));
        }

        public LuaTable toTable() {
            LuaTable table = new LuaTable();
            table.set("name", name);
            if (!children.isEmpty()) {
                var childTable = new LuaTable();
                for (int i = 0; i < children.size(); i++) {
                    childTable.set(i+1, children.get(i).toTable());
                }
                table.set("children", childTable);
            }
            if (!properties.isEmpty()) {
                var props = new LuaTable();
                for (var entry : properties.entrySet())
                    props.set(entry.getKey(), entry.getValue() == null ? LuaValue.NIL : entry.getValue());
                table.set("props", props);
            }
            return table;
        }
    }
}
