package org.web3j.gradle.plugin;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.ConfigureUtil;

class DefaultSoliditySourceSet implements SoliditySourceSet, HasPublicType {

    DefaultSoliditySourceSet(
            final String displayName,
            final SourceDirectorySetFactory setFactory) {

        solidity = setFactory.create(NAME, displayName + " Solidity Sources");
        solidity.getFilter().include("**/*.sol");
        allSolidity = setFactory.create(displayName + " Solidity Sources");
        allSolidity.getFilter().include("**/*.sol");
        allSolidity.source(solidity);
    }

    @Override
    public SourceDirectorySet getSolidity() {
        return solidity;
    }

    @Override
    public SoliditySourceSet solidity(final Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getSolidity());
        return this;
    }

    @Override
    public SoliditySourceSet solidity(final Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getSolidity());
        return this;
    }

    @Override
    public SourceDirectorySet getAllSolidity() {
        return allSolidity;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(SoliditySourceSet.class);
    }

    private final SourceDirectorySet solidity;
    private final SourceDirectorySet allSolidity;
}
